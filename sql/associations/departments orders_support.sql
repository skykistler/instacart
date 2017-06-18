UPDATE departments
SET orders_support = b.orders_support

FROM (
	SELECT department_id, orders_support = COUNT(1)
	FROM departments d
	LEFT JOIN orders o
		ON EXISTS (
			SELECT 1
			FROM order_products op
			LEFT JOIN products p
				ON op.product_id = p.product_id
			WHERE op.order_id = o.order_id
			AND p.department_id = d.department_id
		)
	GROUP BY department_id
) as b

WHERE b.department_id = departments.department_id