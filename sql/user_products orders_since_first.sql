UPDATE user_products

SET  orders_since_first = b.orders_since_first

FROM (
	
	select up1.user_id
		  ,up1.product_id
		  ,orders_since_first = u.total_orders - o1.order_number -- Don't include last order (test or train) as an order since

	from user_products up1

	inner join orders o1
		on o1.user_id = up1.user_id

	join users u
		on u.user_id = up1.user_id

	inner join order_products op1
		on op1.order_id = o1.order_id
		and op1.product_id = up1.product_id
		and op1.reordered = 0

) AS b

WHERE user_products.user_id    = b.user_id 
AND   user_products.product_id = b.product_id
 