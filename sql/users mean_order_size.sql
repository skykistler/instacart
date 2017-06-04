update users
set mean_order_size = b.mean_order_size

from (

select user_id
      ,mean_order_size = avg(order_size) 

from (

select order_size = count(1)
	  ,o.user_id 
from order_products op
join orders o 
	on o.order_id = op.order_id

group by op.order_id, o.user_id
) as a

group by user_id
) as b

where users.user_id = b.user_id