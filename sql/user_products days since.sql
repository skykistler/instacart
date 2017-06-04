UPDATE user_products

SET  orders_since_last = b.orders_since_last_product_order
	,days_since_last   = b.days_since_last_product_order

FROM (
	
	select up1.user_id
		  ,up1.product_id
		  ,orders_since_last_product_order = count(order_id) - 1 -- Don't include last order (test or train) as an order since
		  ,days_since_last_product_order   = sum(days_since_prior_order) -- Include the last order days_prior (even if test or train)

	from user_products up1

	inner join orders o1
		on o1.user_id = up1.user_id
		--and eval_set = 'prior'
		and order_number > (
			SELECT TOP 1 o2.order_number
			FROM order_products op2
			INNER JOIN orders o2
				ON  op2.order_id   = o2.order_id 
				AND o2.user_id     = up1.user_id 
				AND op2.product_id = up1.product_id
			ORDER BY o2.order_number DESC
		)
	
	group by up1.user_id, up1.product_id

) AS b

WHERE user_products.user_id    = b.user_id 
AND   user_products.product_id = b.product_id
