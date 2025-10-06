package com.example.bookingservice.lock;

import com.example.bookingservice.dto.ActiveHold;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
                    local holdId = ARGV[1]
                    local released = 0
                    for i = 1, #KEYS do
                      if redis.call('GET', KEYS[i]) == holdId then
                        redis.call('DEL', KEYS[i])
                        released = released + 1
                      end
                    end
                    return released
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
        List<Object> args = List.of(holdId.toString());
        Long released = redis.execute(
                new DefaultRedisScript<>(LUA_RELEASE, Long.class),
                keys,
                args.toArray()
        );
        redis.delete(holdKey(holdId));
    }


    public Map<UUID, ActiveHold> activeHolds(UUID eventId) {
        String pattern = "seat:" + eventId + ":*";
        var keys = redis.keys(pattern);

        Map<UUID, List<Integer>> seatsByHold = new HashMap<>();
        Map<UUID, Long> minTtlMsByHold = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                String holdIdStr = redis.opsForValue().get(key);
                if (holdIdStr == null) continue;

                UUID holdId;
                try { holdId = UUID.fromString(holdIdStr); }
                catch (Exception ignore) { continue; }

                int seat = Integer.parseInt(key.substring(key.lastIndexOf(':') + 1));

                seatsByHold.computeIfAbsent(holdId, k -> new ArrayList<>()).add(seat);

                Long ttlMs = redis.getExpire(key, TimeUnit.MILLISECONDS);
                if (ttlMs != null && ttlMs > 0) {
                    minTtlMsByHold.merge(holdId, ttlMs, Math::min);
                }
            }
        }

        Map<UUID, ActiveHold> out = new HashMap<>();
        var now = Instant.now();
        for (var e : seatsByHold.entrySet()) {
            UUID holdId = e.getKey();
            List<Integer> seats = e.getValue();
            Long ttlMs = minTtlMsByHold.get(holdId);
            out.put(holdId, new ActiveHold(
                    holdId,
                    seats.stream().sorted().toList(),
                    (ttlMs == null || ttlMs <= 0) ? null : now.plusMillis(ttlMs)
            ));
        }
        return out;
    }

}
