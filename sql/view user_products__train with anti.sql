IF Object_ID('user_products__train') IS NOT NULL
    DROP VIEW user_products__train
GO

CREATE VIEW user_products__train AS

SELECT up.user_id
      ,up.product_id
	  ,o.order_id
	  ,reordered = train_reordered
	  ,product_affinity = up.times_ordered / cast(u.total_orders as real)
	  ,order_days_since_order = o.days_since_prior_order / coalesce(nullif(u.mean_days_btwn,0), .01)
	  ,o.order_dow
	  ,o.order_hour_of_day
	  ,o.order_number
	  ,p.aisle_id
	  ,department_affinity = ud.affinity
	  ,adj_schedule_metric = COALESCE(up.adj_schedule_metric, 1)
	  ,reorder_rate = COALESCE(up.reorder_rate, 1)
	  ,up.reorder_age
	  ,up.mean_hr
	  ,sd_days_btwn = COALESCE(up.sd_days_btwn, 0)
	  
	  ,p_avg_mean_hr			   = COALESCE(p.avg_mean_hr, 0)
	  ,p_avg_schedule_metric       = COALESCE(p.avg_schedule_metric, 0)
	  ,p_avg_reorder_rate          = COALESCE(p.avg_reorder_rate, 0)
	  ,p_avg_reorder_age           = COALESCE(p.avg_reorder_age, 0)
	  ,p_stop_rate                 = COALESCE(p.stop_rate, 0)
	  ,p_avg_sd_days_btwn          = COALESCE(p.avg_sd_days_btwn, 0)

	  ,anti_p1_adj_support         = COALESCE(anti_p1.adj_support, 0)
	  ,anti_p1_aisle_id            = COALESCE(anti_p1.aisle_id, 0)
	  ,anti_p1_avg_schedule_metric = COALESCE(anti_p1.avg_schedule_metric, 0)
	  ,anti_p1_avg_reorder_rate    = COALESCE(anti_p1.avg_reorder_rate, 0)
	  ,anti_p1_avg_reorder_age     = COALESCE(anti_p1.avg_reorder_age, 0)
	  ,anti_p1_stop_rate           = COALESCE(anti_p1.stop_rate, 0)
	 
FROM user_products up
INNER JOIN orders o
	ON o.user_id = up.user_id
	AND o.eval_set = 'train'

LEFT JOIN products p
	ON p.product_id = up.product_id

LEFT JOIN users u
	ON u.user_id = up.user_id

LEFT OUTER JOIN user_departments ud
	ON ud.user_id = u.user_id
	AND ud.department_id = p.department_id

OUTER APPLY (
	SELECT anti_assc.adj_support
	      ,products.product_id
		  ,products.aisle_id
		  ,products.avg_reorder_rate
		  ,products.avg_schedule_metric
		  ,products.avg_reorder_age
		  ,products.stop_rate
	FROM products_anti_assc_2 anti_assc

	LEFT JOIN products 
		ON anti_assc.product_id_2 = products.product_id 

	WHERE anti_assc.product_id_1 = up.product_id 
	AND anti_assc.rank <= 10
	AND EXISTS (
		SELECT 1
		FROM user_products anti_up
		WHERE anti_up.user_id = up.user_id
		AND anti_up.product_id = anti_assc.product_id_2
	)

	ORDER BY rank ASC
	OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY
) AS anti_p1

WHERE up.train_reordered IS NOT NULL

--ORDER BY user_id, product_id
GO