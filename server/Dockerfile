# 构建层：JDK 21 + Maven Wrapper（多阶段构建，体积收在运行层之外）
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY mvnw pom.xml ./
COPY .mvn ./.mvn
# 先解析依赖建立缓存层（失败不阻断，随后 package 仍会补全）
RUN ./mvnw -B -q dependency:go-offline || true
COPY src ./src
RUN ./mvnw -B -q package -DskipTests

# 运行层：JRE 21，非 root 运行（uid/gid 1000）
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S -g 1000 sinomed && adduser -S -u 1000 -G sinomed sinomed \
    && mkdir -p /app/logs /app/config /app/data \
    && chown -R sinomed:sinomed /app
WORKDIR /app
COPY --from=build /build/target/sinomed-server.jar /app/server.jar
USER sinomed
ENV JAVA_OPTS=""
EXPOSE 2347
VOLUME /app/logs
VOLUME /app/data
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/server.jar"]
