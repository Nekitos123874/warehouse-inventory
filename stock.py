# stock.py — операции прихода/расхода
from models import Product

_stock = {}


def stock_in(product_id, quantity):
    _stock[product_id] = _stock.get(product_id, 0) + quantity


def stock_out(product_id, quantity):
    current = _stock.get(product_id, 0)
    if current < quantity:
        raise ValueError("Not enough stock")
    _stock[product_id] = current - quantity


def get_stock(product_id):
    return _stock.get(product_id, 0)