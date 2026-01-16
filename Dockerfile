# 使用龙蜥社区的 OpenJDK 作为基础
FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com/openanolis/openjdk:17-8.6 AS builder

# 安装 Maven
RUN yum install -y maven

WORKDIR /app
COPY lucene-shard-analyzer/pom.xml .
COPY lucene-shard-analyzer/src ./src
RUN mvn clean package -DskipTests

# 运行阶段（可以用同一个镜像）
FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com/openanolis/openjdk:17-8.6
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]