# syntax=docker/dockerfile:1

# ---- build stage (module-local) ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy the module contents (build context is the module folder)
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon clean bootJar

# ---- runtime ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# Be robust to version names: pick the only jar produced
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
