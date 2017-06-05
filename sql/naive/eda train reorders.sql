select count(1) as up_1_times_ordered
from user_products 
where times_ordered = 1;

select train_1_times_ordered = count(1) 
from user_products up
inner join orders o on o.user_id = up.user_id and o.eval_set='train' 
inner join order_products__train train on train.order_id = o.order_id and train.product_id = up.product_id
where up.times_ordered = 1;

-----------------------------------------------

select count(1) as up_2_times_ordered
from user_products 
where times_ordered > 1;

select train_2_times_ordered = count(1) 
from user_products up
inner join orders o on o.user_id = up.user_id and o.eval_set='train' 
inner join order_products__train train on train.order_id = o.order_id and train.product_id = up.product_id
where up.times_ordered >1;

-----------------------------------------------

select train_no_times_ordered = count(1) 
from order_products__train train 
inner join orders o on train.order_id = o.order_id 
where not exists (select 1 from user_products up where train.product_id = up.product_id and o.user_id = up.user_id);