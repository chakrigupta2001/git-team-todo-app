FROM eclipse-temurin:17-jre

LABEL maintainer="chakrigupta"
LABEL application="git-team-todo-app"
LABEL version="1.0"

WORKDIR /app

ENV APP_HOME=/app

RUN mkdir -p /app/logs

COPY target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]