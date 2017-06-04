UPDATE products
SET avg_mean_hr         = b.avg_mean_hr
   ,avg_schedule_metric = b.avg_schedule_metric
   ,avg_reorder_rate    = b.avg_reorder_rate
   ,avg_reorder_age     = b.avg_reorder_rate
   ,stop_rate           = b.stop_rate

FROM (
	SELECT product_id
	      ,avg_mean_hr         = avg(mean_hr)
	      ,avg_schedule_metric = avg(adj_schedule_metric)
	      ,avg_reorder_rate    = avg(reorder_rate)
		  ,avg_reorder_age     = avg(reorder_age)
		  ,stop_rate           = (SELECT CAST(COUNT(1) as real) FROM user_products up2 WHERE up2.product_id = up.product_id AND up2.stop_rate < 0) / COUNT(1)

	FROM user_products up

	GROUP BY up.product_id
) AS b

WHERE b.product_id = products.product_id