UPDATE user_products

SET  min_days_btwn  = b.min_days_btwn
	,max_days_btwn  = b.max_days_btwn
	,mean_days_btwn = b.mean_days_btwn

FROM (
	SELECT
		user_id
	   ,product_id
	   ,min_days_btwn  = min(days_since_prior_product_order)
	   ,max_days_btwn  = max(days_since_prior_product_order)
	   ,mean_days_btwn = avg(cast(days_since_prior_product_order as real))
	FROM (
		select o1.user_id
			  ,op1.product_id
			  ,days_since_prior_product_order = sum(o3.days_since_prior_order)

		from order_products op1

		join orders o1
			on o1.order_id = op1.order_id
	
		right outer join orders o3
			on o3.user_id = o1.user_id
			and o3.order_number <= o1.order_number
			and o3.order_number > (
				SELECT TOP 1 o2.order_number
				FROM order_products op2
				INNER JOIN orders o2
					ON op2.order_id = o2.order_id 
					AND o2.user_id = o1.user_id 
					AND op2.product_id = op1.product_id
					AND o2.order_number < o1.order_number
				ORDER BY o2.order_number DESC
			)

		group by o1.user_id, op1.product_id, o1.order_id

	) AS a
	GROUP BY user_id, product_id

) AS b

WHERE user_products.user_id    = b.user_id 
AND   user_products.product_id = b.product_id
