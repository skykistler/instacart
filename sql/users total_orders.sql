update users
set total_orders = b.total_orders

from (

select user_id
      ,total_orders = max(order_number)  - 1

from orders
group by user_id

) as b

where users.user_id = b.user_id