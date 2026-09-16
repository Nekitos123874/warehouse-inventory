# products_repo.py — хранилище товаров
from models import Product

_products = {}
_next_id = 1


def add_product(name, sku, price):
    global _next_id
    product = Product(_next_id, name, sku, price)
    _products[_next_id] = product
    _next_id += 1
    return product


def list_products():
    return list(_products.values())