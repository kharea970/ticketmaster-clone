# ---- build ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon clean bootJar

# ---- run ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport"
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
