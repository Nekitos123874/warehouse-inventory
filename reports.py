# reports.py — отчёты
from products_repo import list_products
from stock import get_stock


def stock_report():
    report = []
    for product in list_products():
        report.append({
            "sku": product.sku,
            "name": product.name,
            "quantity": get_stock(product.id),
        })
    return report