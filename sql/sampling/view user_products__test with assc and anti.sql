IF Object_ID('user_products__test') IS NOT NULL
    DROP VIEW user_products__test
GO

CREATE VIEW user_products__test AS

SELECT up.user_id
      ,up.product_id
	  ,o.order_id
	  ,reordered = up.train_reordered
	  ,product_affinity = up.affinity
	  ,order_adj_days_since_prior = adj_days_since_prior
	  ,o.order_number
	  ,aisle_affinity      = ua.affinity
	  ,department_affinity = ud.affinity
	  ,adj_schedule_metric = COALESCE(up.adj_schedule_metric, 1)
	  ,reorder_rate = COALESCE(up.reorder_rate, 1)
	  ,reorder_age  = up.reorder_age
	  ,diff_hr      = up.diff_hr
	  ,sd_days_btwn = COALESCE(up.sd_days_btwn, 0)
	  ,stop_rate    = COALESCE(up.stop_rate, 0)
	  
	  ,p_avg_schedule_metric       = COALESCE(p.avg_schedule_metric, 0)
	  ,p_avg_reorder_rate          = COALESCE(p.avg_reorder_rate, 0)
	  ,p_stop_rate                 = COALESCE(p.stop_rate, 0)
	  ,p_avg_sd_days_btwn          = COALESCE(p.avg_sd_days_btwn, 0)

	  ,assc_p1_adj_support     = COALESCE(assc_p1.adj_support, 0)
	  ,assc_p1_affinity        = COALESCE(assc_up.affinity, 0)
	  ,assc_up_aisle_affinity  = COALESCE(assc_aisle.affinity, 0)
	  ,assc_up_schedule_metric = COALESCE(assc_up.adj_schedule_metric, 0)
	  ,assc_up_reorder_rate    = COALESCE(assc_up.reorder_rate, 0)
	  ,assc_up_reorder_age     = COALESCE(assc_up.reorder_age, 0)
	  ,assc_up_stop_rate       = COALESCE(assc_up.stop_rate, 0)
	  ,assc_up_sd_days_btwn    = COALESCE(assc_up.sd_days_btwn, 0)
	  ,assc_up_diff_hr         = COALESCE(assc_up.diff_hr, 0)

	  ,anti_p1_adj_support     = COALESCE(anti_p1.adj_support, 0)
	  ,anti_p1_affinity        = COALESCE(anti_up.affinity, 0)
	  ,anti_up_aisle_affinity  = COALESCE(anti_aisle.affinity, 0)
	  ,anti_up_schedule_metric = COALESCE(anti_up.adj_schedule_metric, 0)
	  ,anti_up_reorder_rate    = COALESCE(anti_up.reorder_rate, 0)
	  ,anti_up_reorder_age     = COALESCE(anti_up.reorder_age, 0)
	  ,anti_up_stop_rate       = COALESCE(anti_up.stop_rate, 0)
	  ,anti_up_sd_days_btwn    = COALESCE(anti_up.sd_days_btwn, 0)
	  ,anti_up_diff_hr         = COALESCE(anti_up.diff_hr, 0)
	 
FROM user_products up
INNER JOIN orders o
	ON o.user_id = up.user_id
	AND o.eval_set = 'test'

LEFT JOIN products p
	ON p.product_id = up.product_id

LEFT JOIN users u
	ON u.user_id = up.user_id

LEFT OUTER JOIN user_departments ud
	ON ud.user_id = u.user_id
	AND ud.department_id = p.department_id

LEFT OUTER JOIN user_aisles ua
	ON ua.user_id = u.user_id
	AND ua.aisle_id = p.aisle_id

OUTER APPLY (
	SELECT anti_assc.adj_support
	      ,product_id = anti_assc.product_id_2
	FROM products_anti_assc_2 anti_assc

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

LEFT OUTER JOIN user_products anti_up
	ON anti_up.user_id = up.user_id
	AND anti_up.product_id = anti_p1.product_id

LEFT OUTER JOIN user_aisles anti_aisle
	ON anti_aisle.user_id = up.user_id
	AND anti_aisle.aisle_id = (SELECT aisle_id FROM products anti_p WHERE anti_p.product_id = anti_p1.product_id)

OUTER APPLY (
	SELECT assc.adj_support
	      ,product_id = assc.product_id_2
	FROM products_assc_2 assc

	WHERE assc.product_id_1 = up.product_id 
	AND assc.rank <= 10
	AND EXISTS (
		SELECT 1
		FROM user_products assc_up
		WHERE assc_up.user_id = up.user_id
		AND assc_up.product_id = assc.product_id_2
	)

	ORDER BY rank ASC
	OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY
) AS assc_p1

LEFT OUTER JOIN user_products assc_up
	ON assc_up.user_id = up.user_id
	AND assc_up.product_id = assc_p1.product_id

LEFT OUTER JOIN user_aisles assc_aisle
	ON assc_aisle.user_id = up.user_id
	AND assc_aisle.aisle_id = (SELECT aisle_id FROM products assc_p WHERE assc_p.product_id = assc_p1.product_id)

WHERE up.train_reordered IS NULL

--ORDER BY user_id, product_id
GO