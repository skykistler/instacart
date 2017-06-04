DELETE FROM order_products__pred;
INSERT INTO order_products__pred (order_id, product_id)

SELECT order_id, product_id
FROM (

	SELECT o.order_id
		  ,up.*

	FROM user_products up
	INNER JOIN orders o
		ON o.user_id = up.user_id
		AND o.eval_set = 'train'
		
	LEFT JOIN users u
		ON u.user_id = up.user_id

	WHERE  --(days_since_last < up.max_days_btwn OR orders_since_last * u.mean_days_btwn < up.mean_days_btwn + u.mean_days_btwn) AND (
	  (  (times_ordered >= orders_since_last and orders_since_last < total_orders / 2) 
	 OR (times_ordered > total_orders/4 AND abs(up.days_since_last - up.mean_days_btwn) <= o.days_since_prior_order)
--	OR	(times_ordered > 5 AND abs(up.days_since_last - o.days_since_prior_order) <= u.mean_days_btwn + 7)
	--OR (times_ordered > 5 AND abs(up.days_since_last - up.mean_days_btwn) * (o.days_since_prior_order / u.mean_days_btwn) > (up.mean_days_btwn - up.min_days_btwn))
	--OR  (times_ordered > 5 AND mean_cart_importance <= 1/mean_order_size)
	)

) AS up

WHERE NOT EXISTS (SELECT 1 FROM order_products__pred pred WHERE pred.order_id = order_id AND pred.product_id = up.product_id)