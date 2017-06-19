SELECT_REVERSE_BASKETS <- "select
  o.user_id, o.order_number, op.product_id, days_to_next_order = o2.days_since_prior_order

from order_products op

join (
    select op_count.product_id
          ,support = count(1)
    from order_products op_count
    group by product_id
) as p
  on p.product_id = op.product_id

join orders o
  on o.order_id = op.order_id

left join orders o2
  on o2.user_id = o.user_id
  and o2.order_number = o.order_number + 1

join users u
	on u.user_id = o.user_id

where support >= 1000

order by o.user_id, order_number desc, p.support desc;"

db %>% 
  sqlQuery(SELECT_REVERSE_BASKETS, rows_at_time = 1024) %>%
  write.csv(
    'data/facts/baskets reverse order min 1000.csv',
    quote=F,
    row.names=F
  )