# models.py — модели домена
from datetime import datetime


class Product:
    """Товар на складе."""

    def __init__(self, id, name, sku, price):
        self.id = id
        self.name = name
        self.sku = sku
        self.price = price
        self.created_at = datetime.now()