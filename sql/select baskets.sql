select
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

where support >= 1000

order by user_id, order_number, p.support desc;