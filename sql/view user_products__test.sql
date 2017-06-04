CREATE VIEW user_products__test AS

SELECT up.user_id
      ,up.product_id
	  ,o.order_id
	  ,reordered = -1
	  ,order_days_since_order = o.days_since_prior_order / coalesce(nullif(u.mean_days_btwn,0), .01)
	  ,o.order_dow
	  ,o.order_hour_of_day
	  ,o.order_number
	  ,p.aisle_id
	  ,up.adj_schedule_metric
	  ,up.reorder_rate
	  ,up.reorder_age
	  ,up.mean_hr
	  ,p.avg_schedule_metric
	  ,p.avg_reorder_rate
	  ,p.avg_reorder_age
	  ,p.stop_rate
	 
FROM user_products up
INNER JOIN orders o
	ON o.user_id = up.user_id
	AND o.eval_set = 'test'

LEFT JOIN products p
	ON p.product_id = up.product_id

LEFT JOIN users u
	ON u.user_id = up.user_id