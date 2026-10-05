# 构建层：JDK 21 + Maven Wrapper（多阶段构建，体积收在运行层之外）
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY mvnw pom.xml ./
COPY .mvn ./.mvn
# 可选构建源（出境受限时传国内源，留空走官方源）：
#   MVN_DIST_MIRROR  —— Maven 发行版镜像基址（改写 maven-wrapper.properties 的 distributionUrl；
#                       名字刻意避开 MVNW_REPOURL，避免作为环境变量被 wrapper 二次改写）
#   MAVEN_MIRROR_URL —— 依赖仓库地址（写入 settings.xml 的 mirror，mirrorOf=*）
ARG MVN_DIST_MIRROR=""
ARG MAVEN_MIRROR_URL=""
RUN if [ -n "$MVN_DIST_MIRROR" ]; then \
        sed -i "s#^distributionUrl=.*apache-maven-\([0-9.]*\)-bin\.zip#distributionUrl=${MVN_DIST_MIRROR}/org/apache/maven/apache-maven/\1/apache-maven-\1-bin.zip#" .mvn/wrapper/maven-wrapper.properties; \
    fi && \
    if [ -n "$MAVEN_MIRROR_URL" ]; then \
        mkdir -p /root/.m2 && \
        echo "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"><mirrors><mirror><id>build-mirror</id><name>build mirror</name><url>${MAVEN_MIRROR_URL}</url><mirrorOf>*</mirrorOf></mirror></mirrors></settings>" > /root/.m2/settings.xml; \
    fi
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
