\# Warehouse Inventory API



Простое REST API для учёта складских товаров

\## Endpoint'ы

\- `GET /products` — список товаров

\- `GET /health` — проверка работоспособности



\## Требования

\- Java 17 (JDK)

\- Docker Desktop



\## Локальный запуск

```powershell

javac App.java

java App

```



\## Сборка JAR

```powershell

jar cfe app.jar App App.class

```



\## Сборка Docker-образа

```powershell

docker build -t warehouse-api:latest .

```



\## Запуск контейнера

```powershell

docker run -d -p 8080:8080 --name warehouse-container warehouse-api:latest

```



\## Проверка

```powershell

curl.exe http://localhost:8080/products

docker ps

docker logs warehouse-container

```



\## Остановка и удаление

```powershell

docker stop warehouse-container

docker rm warehouse-container

docker rmi warehouse-api:latest

```

