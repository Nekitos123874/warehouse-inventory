\# Project Notes



\## Идея

Учёт складских товаров: приход, расход, остатки, категории.



\## Сущности

\- Product (товар): id, name, sku, category, price

\- Stock (остаток): product\_id, quantity, warehouse\_id

\- Operation (операция): id, product\_id, type (in/out), quantity, date



\## Дальнейшие шаги

1\. Спроектировать API

2\. Реализовать модели

3\. Настроить хранение

4\. Добавить отчёты

