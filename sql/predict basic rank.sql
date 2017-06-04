--DELETE FROM order_products__pred;
INSERT INTO order_products__pred (order_id, product_id)

SELECT order_id, product_id
FROM (

	SELECT o.order_id
		  ,up.product_id
		  ,up.user_id
		  ,rank = ROW_NUMBER() OVER (
			  PARTITION BY up.user_id
			  ORDER BY times_ordered DESC, orders_since_last
		  )

	FROM user_products up
	INNER JOIN orders o
		ON o.user_id = up.user_id
		AND o.eval_set = 'train'

) AS up
LEFT JOIN users u
	ON u.user_id = up.user_id

WHERE rank < u.mean_order_size /2