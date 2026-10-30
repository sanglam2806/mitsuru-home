# Runtime base image. An ARG used in FROM must be declared before the first FROM.
# The JDK image can stand in for the JRE one if it's already pulled: --build-arg JAVA_RUNTIME_IMAGE=eclipse-temurin:25-jdk
ARG JAVA_RUNTIME_IMAGE=eclipse-temurin:25-jre

# build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src
# The cache mount keeps the Gradle distribution and dependencies between builds (outside the image),
# so only the first build downloads them. Tests run in CI (./gradlew test), not here.
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon bootJar -x test
# Split the fat jar into layers (dependencies ~64 MB, application code ~100 KB),
# so a code change only produces a new small top layer instead of a whole new jar.
RUN java -Djarmode=tools -jar build/libs/admin-*.jar extract --layers --launcher --destination extracted

# run stage
FROM ${JAVA_RUNTIME_IMAGE}
WORKDIR /app
# run as an unprivileged user instead of root
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
# least to most frequently changed, so the unchanged layers are reused between builds and pulls
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./
USER spring
EXPOSE  8080
# MaxRAMPercentage sizes the heap from the container's memory limit instead of the host's;
# more JVM flags can be passed at runtime through the JAVA_TOOL_OPTIONS environment variable
ENTRYPOINT [ "java", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher" ]
