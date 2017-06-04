DELETE FROM order_products__pred;


INSERT INTO order_products__pred (order_id, product_id)

SELECT order_id, product_id

FROM (

	SELECT o.order_id
		  ,product_id = up.product_id

	FROM user_products up
	INNER JOIN orders o
		ON o.user_id = up.user_id
		AND o.eval_set = 'test'
		
	LEFT JOIN users u
		ON u.user_id = up.user_id

	WHERE -- benchmark
	(times_ordered > orders_since_last and orders_since_last < total_orders / 2) 
	 OR (times_ordered > total_orders/4 AND abs(up.days_since_last - up.mean_days_btwn) <= o.days_since_prior_order)


) AS up

WHERE NOT EXISTS (SELECT 1 FROM order_products__pred pred WHERE pred.order_id = order_id AND pred.product_id = product_id)


GROUP BY order_id, product_id;

------------

INSERT INTO order_products__pred (order_id, product_id)

SELECT order_id, product_id

FROM (

	SELECT o2.order_id
	      ,product_id = assc.product_id_2

	FROM order_products op1

	INNER JOIN orders o1
		ON o1.order_id = op1.order_id

	INNER JOIN orders o2
		ON o2.user_id = o1.user_id
		AND o2.eval_set = 'test'
		AND o1.order_number + 1 = o2.order_number

	LEFT JOIN user_products up
		ON up.user_id = o1.user_id
		AND up.product_id = op1.product_id

	LEFT JOIN users u
		ON u.user_id = up.user_id

	INNER JOIN products_anti_assc_2 assc
		ON assc.product_id_1 = op1.product_id
		AND o2.days_since_prior_order < up.mean_days_btwn
		AND assc.support > 20000
		AND assc.product_id_2 IN (
			SELECT TOP 2 assc2.product_id_2
			FROM products_anti_assc_2 assc2
			WHERE assc.product_id_1 = assc2.product_id_1 
			ORDER BY support DESC
		)



) AS up

WHERE NOT EXISTS (SELECT 1 FROM order_products__pred pred WHERE pred.order_id = up.order_id AND pred.product_id = up.product_id)

GROUP BY order_id, product_id;