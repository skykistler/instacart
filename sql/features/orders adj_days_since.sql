UPDATE orders
SET adj_days_since_prior = b.adj_days_since_prior

FROM (
	SELECT order_id
	      ,adj_days_since_prior = o.days_since_prior_order / coalesce(nullif(u.mean_days_btwn,0), .01)

	FROM orders o
	JOIN users u
		ON u.user_id = o.user_id

) as b

WHERE b.order_id = orders.order_id;