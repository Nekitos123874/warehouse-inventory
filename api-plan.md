\# API Plan



\## Эндпоинты



\### Товары

\- `GET /products` — список товаров

\- `POST /products` — создать товар

\- `GET /products/{id}` — товар по ID

\- `PUT /products/{id}` — обновить

\- `DELETE /products/{id}` — удалить



\### Склад

\- `POST /stock/in` — приход товара

\- `POST /stock/out` — расход товара

\- `GET /stock/{product\_id}` — остаток по товару



\### Отчёты

\- `GET /reports/stock` — отчёт по остаткам

\- `GET /reports/movement` — отчёт по движению

