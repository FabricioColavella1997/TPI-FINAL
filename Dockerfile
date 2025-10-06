FROM openjdk:17-jdk-slim

RUN addgroup --system spring && adduser --system spring --ingroup spring

workdir /app

COPY target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
