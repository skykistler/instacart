UPDATE user_products
SET affinity = b.affinity

FROM (
	SELECT up.user_id
	      ,product_id
		  ,affinity =  up.times_ordered / cast(u.total_orders as real)
	FROM user_products up
	LEFT JOIN users u
		ON u.user_id = up.user_id

) as b

WHERE b.user_id = user_products.user_id AND b.product_id = user_products.product_id