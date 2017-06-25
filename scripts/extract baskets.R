SELECT_BASKETS <- "select
  o.user_id, o.order_number, op.product_id, o.days_since_prior_order

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

join users u
on u.user_id = o.user_id

order by o.user_id, order_number, p.support desc;"

db %>% 
  sqlQuery(SELECT_BASKETS, rows_at_time = 1024) %>%
  write.csv(
    'data/facts/baskets.csv',
    quote=F,
    row.names=F
  )