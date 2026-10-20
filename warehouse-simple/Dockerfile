FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем драйвер БД
COPY lib/postgresql-42.7.2.jar /app/lib/postgresql.jar

# Копируем наше приложение
COPY app.jar app.jar

EXPOSE 8080

# Запускаем с указанием classpath, чтобы Java видела драйвер
ENTRYPOINT ["java", "-cp", "app.jar:/app/lib/postgresql.jar", "App"]