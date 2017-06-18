UPDATE aisles
SET orders_support = b.orders_support

FROM (
	SELECT aisle_id, orders_support = COUNT(1)
	FROM aisles a
	LEFT JOIN orders o
		ON EXISTS (
			SELECT 1
			FROM order_products op
			LEFT JOIN products p
				ON op.product_id = p.product_id
			WHERE op.order_id = o.order_id
			AND p.aisle_id = a.aisle_id
		)
	GROUP BY aisle_id
) as b

WHERE b.aisle_id = aisles.aisle_id