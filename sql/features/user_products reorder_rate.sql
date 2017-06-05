UPDATE user_products
SET reorder_rate = b.reorder_rate
   ,reorder_age = b.reorder_age
   ,stop_rate = b.stop_rate
   ,adj_schedule_metric = b.adj_schedule_metric

FROM (
	SELECT up.user_id
		  ,product_id
		  ,reorder_rate = (schedule_metric / (total_orders - orders_since_first)) / total_orders
		  ,reorder_age = orders_since_first / cast(total_orders as real)
		  ,stop_rate = (schedule_metric - orders_since_last) / total_orders
		  ,adj_schedule_metric = schedule_metric / total_orders

	FROM user_products up
	JOIN users u
		ON u.user_id = up.user_id
) as b

WHERE user_products.user_id = b.user_id AND user_products.product_id = b.product_id