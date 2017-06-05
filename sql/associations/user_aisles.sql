INSERT INTO user_aisles

SELECT o.user_id
      ,p.aisle_id
	  ,count(1) / (u.total_orders * u.mean_order_size)

FROM orders o
INNER JOIN order_products op
	ON op.order_id = o.order_id
	AND o.eval_set = 'prior'

LEFT JOIN products p
	ON p.product_id = op.product_id

LEFT JOIN users u
	ON u.user_id = o.user_id


GROUP BY o.user_id, p.aisle_id, u.total_orders, u.mean_order_size