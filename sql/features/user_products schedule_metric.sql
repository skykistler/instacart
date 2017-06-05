UPDATE user_products
SET schedule_metric = b.schedule_metric

FROM (
	SELECT up.user_id
		  ,product_id
		  ,schedule_metric = times_ordered * (up.mean_days_btwn / coalesce(nullif(u.mean_days_btwn,0), .01))

	FROM user_products up
	JOIN users u
		ON u.user_id = up.user_id
) as b

WHERE user_products.user_id = b.user_id AND user_products.product_id = b.product_id