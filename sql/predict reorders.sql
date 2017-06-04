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
	
	WHERE stop_rate >= 0 and reorder_age > .4

) AS up

WHERE NOT EXISTS (SELECT 1 FROM order_products__pred pred WHERE pred.order_id = order_id AND pred.product_id = up.product_id)