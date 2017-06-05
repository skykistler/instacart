UPDATE user_products

SET  mean_cart_importance  = b.mean_cart_importance

FROM (
	SELECT
		user_id
	   ,product_id
	   ,mean_cart_importance = avg(cart_importance)

	FROM (
		select o1.user_id
			  ,op1.product_id
			  ,cart_importance = (cast(add_to_cart_order as real) / (SELECT COUNT(1) FROM order_products op2 WHERE op2.order_id = op1.order_id))

		from order_products op1

		join orders o1
			on o1.order_id = op1.order_id

		join users u
			on u.user_id = o1.user_id

	) AS a
	GROUP BY user_id, product_id

) AS b

WHERE user_products.user_id    = b.user_id 
AND   user_products.product_id = b.product_id
