UPDATE user_products
SET diff_hr = b.diff_hr

FROM (
	SELECT up.user_id
	      ,product_id
		  ,diff_hr = (up.mean_hr - o.order_hour_of_day) / 24
	FROM user_products up
	LEFT JOIN orders o
		ON o.user_id = up.user_id
		AND o.eval_set IN ('train', 'test')

) as b

WHERE b.user_id = user_products.user_id AND b.product_id = user_products.product_id