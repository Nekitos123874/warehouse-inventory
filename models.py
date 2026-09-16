# models.py — модели домена (ветка stock)
from datetime import datetime


class Product:
    """Товар на складе."""

    def __init__(self, id, name, sku, price, quantity=0):
        self.id = id
        self.name = name
        self.sku = sku
        self.price = price
        self.quantity = quantity  # добавлено в ветке stock
        self.created_at = datetime.now()