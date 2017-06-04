CREATE VIEW user_products__train AS

SELECT up.user_id
      ,up.product_id
	  ,o.order_id
	  ,reordered = (CASE WHEN train.order_id IS NOT NULL THEN 1 ELSE 0 END)
	  ,order_days_since_order = o.days_since_prior_order / coalesce(nullif(u.mean_days_btwn,0), .01)
	  ,o.order_dow
	  ,o.order_hour_of_day
	  ,o.order_number
	  ,p.aisle_id
	  ,ud.affinity
	  ,adj_schedule_metric = COALESCE(up.adj_schedule_metric, 1)
	  ,reorder_rate = COALESCE(up.reorder_rate, 1)
	  ,up.reorder_age
	  ,up.mean_hr
	  ,sd_days_btwn = COALESCE(up.sd_days_btwn, 0)
	  ,p.avg_schedule_metric
	  ,p.avg_reorder_rate
	  ,p.avg_reorder_age
	  ,p.stop_rate
	  ,avg_sd_days_btwn = COALESCE(p.avg_sd_days_btwn, 0)
	 
FROM user_products up
INNER JOIN orders o
	ON o.user_id = up.user_id
	AND o.eval_set = 'train'

LEFT OUTER JOIN order_products__train train
	ON train.order_id = o.order_id
	AND train.product_id = up.product_id

LEFT JOIN products p
	ON p.product_id = up.product_id

LEFT JOIN users u
	ON u.user_id = up.user_id

LEFT OUTER JOIN user_departments ud
	ON ud.user_id = u.user_id
	AND ud.department_id = p.department_id