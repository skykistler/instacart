insert into user_products (user_id, product_id, times_ordered, earliest_hr, latest_hr, mean_hr)

select
	 u.user_id
	,op.product_id
	,times_ordered = count(op.product_id)
	,earliest_hr   = min(o.order_hour_of_day)
	,latest_hr     = max(o.order_hour_of_day)
	,mean_hr       = avg(cast(o.order_hour_of_day as real))

from users u

join orders o
	on o.user_id = u.user_id
	and o.eval_set = 'prior' -- REAAALLL IMPORTANT!~@!@

join order_products op
	on op.order_id = o.order_id


group by u.user_id, op.product_id
order by u.user_id, op.product_id