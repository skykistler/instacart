UPDATE products
	SET products.orders_support = b.orders_support

FROM (
	SELECT product_id, orders_support = count(1)
	FROM order_products op
	GROUP BY product_id
) as b

WHERE b.product_id = products.product_id