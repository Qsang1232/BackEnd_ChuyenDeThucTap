# 1. Build giai đoạn biên dịch
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Build giai đoạn chạy (Run)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render sẽ cấp port qua biến môi trường PORT, mặc định ta cứ expose 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]