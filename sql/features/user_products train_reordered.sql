UPDATE user_products
SET train_reordered = b.train_reordered

FROM (
	SELECT up.user_id, up.product_id
		  ,train_reordered  = (CASE WHEN train.order_id IS NOT NULL THEN 1 ELSE 0 END)
	FROM user_products up
	INNER JOIN orders o
		ON o.user_id = up.user_id
		AND o.eval_set = 'train'
	LEFT OUTER JOIN order_products__train train
		ON train.order_id = o.order_id
		AND train.product_id = up.product_id
) b

WHERE b.user_id = user_products.user_id AND b.product_id = user_products.product_id