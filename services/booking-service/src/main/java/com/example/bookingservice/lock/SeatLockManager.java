package com.example.bookingservice.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SeatLockManager {

    private final StringRedisTemplate redis;

    @Value("${app.booking.hold-ttl-seconds:600}")
    private long ttlSeconds;

    // Keys:
    //  seat key: event:{eventId}:seat:{seat} -> holdId  (TTL)
    //  hold set: hold:{holdId} -> csv seats   (TTL)
    private String seatKey(UUID eventId, int seat) { return "event:%s:seat:%d".formatted(eventId, seat); }
    private String holdKey(UUID holdId) { return "hold:%s".formatted(holdId); }

    private static final String LUA_HOLD = """
-- KEYS[1..n] = seat keys (e.g., seats:<eventId>:<seat>)
-- ARGV[1] = holdId
-- ARGV[2] = ttlSeconds
local holdId = ARGV[1]
local ttl    = tonumber(ARGV[2])

for i = 1, #KEYS do
  local ok = redis.call('SET', KEYS[i], holdId, 'EX', ttl, 'NX')
  if not ok then
    -- rollback everything we set in this loop
    for j = 1, i - 1 do
      if redis.call('GET', KEYS[j]) == holdId then
        redis.call('DEL', KEYS[j])
      end
    end
    return 0
  end
end

return 1
    """;

    private static final String LUA_RELEASE = """
    -- ARGV[1] = holdId, seats keys in ARGV[2..n]
    local holdId = ARGV[1]
    for i=2,#ARGV do
      local key = ARGV[i]
      local v = redis.call('GET', key)
      if v == holdId then
        redis.call('DEL', key)
      end
    end
    return 1
    """;

    public UUID tryHold(UUID eventId, List<Integer> seats) {
        UUID holdId = UUID.randomUUID();

        // KEYS = one key per seat
        List<String> keys = seats.stream()
                .map(s -> seatKey(eventId, s))  // e.g., seats:<eventId>:<s>
                .toList();

        // ARGV = holdId, ttlSeconds
        String ttl = String.valueOf(ttlSeconds);

        Long ok = redis.execute(
                new DefaultRedisScript<>(LUA_HOLD, Long.class),
                keys,                                     // <- KEYS
                holdId.toString(), ttl                    // <- ARGV[1], ARGV[2]
        );

        if (ok == null || ok != 1L) return null;

        // store reverse index for easy release / SSE, with TTL
        String seatCsv = seats.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        redis.opsForValue().set(holdKey(holdId), seatCsv, java.time.Duration.ofSeconds(ttlSeconds));
        return holdId;
    }

    public void release(UUID eventId, UUID holdId, List<Integer> seats) {
        List<String> keys = new ArrayList<>();
        for (int s : seats) keys.add(seatKey(eventId, s));
        redis.execute(new DefaultRedisScript<>(LUA_RELEASE, Boolean.class),
                Collections.emptyList(),
                concat(List.of(holdId.toString()), keys));
        redis.delete(holdKey(holdId));
    }

    private static <T> List<T> concat(List<T> a, List<T> b) {
        var r = new ArrayList<T>(a.size() + b.size());
        r.addAll(a); r.addAll(b); return r;
    }
}
