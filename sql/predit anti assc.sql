DELETE FROM order_products__pred;


INSERT INTO order_products__pred (order_id, product_id)

SELECT order_id, product_id

FROM (

	SELECT o.order_id
		  ,product_id = COALESCE(anti_assc.product_id_2, up.product_id)

	FROM user_products up
	INNER JOIN orders o
		ON o.user_id = up.user_id
		AND o.eval_set = 'train'
		
	LEFT JOIN users u
		ON u.user_id = up.user_id

	LEFT OUTER JOIN products_anti_assc_2 anti_assc
		ON anti_assc.product_id_1 = up.product_id
		AND orders_since_last > 0 -- might churn this product
		AND anti_assc.product_id_2 = (
			SELECT TOP 1 paac2.product_id_2
			FROM products_anti_assc_2 paac2
			WHERE paac2.product_id_1 = up.product_id
			AND support > 10000
			ORDER BY support DESC
		)

	WHERE -- scheduled to purchase product
	(times_ordered > total_orders/4 AND abs(up.days_since_last - up.mean_days_btwn) <= o.days_since_prior_order)
	-- likely to reorder product
	OR (times_ordered > orders_since_last and orders_since_last < total_orders / 2) 


) AS up

WHERE NOT EXISTS (SELECT 1 FROM order_products__pred pred WHERE pred.order_id = order_id AND pred.product_id = product_id)


GROUP BY order_id, product_id;