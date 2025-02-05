select 
	reordered, 
	not_reordered, 
	precision = cast(reordered as real) / (reordered + not_reordered),
	recall = cast(reordered as real) / (training_reorders)
from (
(
select not_reordered = count(1)
from user_products up
left join user_potential_products upp
	on upp.user_id = up.user_id
	and upp.y = up.product_id
where upp.support is not null and up.train_reordered = 0
) as c

cross apply (
select reordered = count(1)
from user_products up
left join user_potential_products upp
	on upp.user_id = up.user_id
	and upp.y = up.product_id
where upp.support is not null and up.train_reordered = 1
) as b

cross apply (
select training_reorders = count(1)
from user_products up
where up.train_reordered = 1 
and user_id in (
	select distinct user_id
	from user_potential_products upp
)
) as d
) ;

select total_training_product_orders = count(1)
from user_products up
left join user_potential_products upp
	on upp.user_id = up.user_id
	and upp.y = up.product_id
where up.train_reordered = 1;

select users_calculated = count(1)
from users u
where exists (
	select 1
	from user_potential_products upp
	where upp.user_id = u.user_id
);

select training_users_calculated = count(1)
from users u
where exists (
	select 1
	from user_potential_products upp
	inner join user_products up
		on up.user_id = upp.user_id
		and up.product_id = upp.y
		and up.train_reordered is not null
	where upp.user_id = u.user_id
);

select training_products_covered = count(1)
from user_products up1
where train_reordered = 1 and 
up1.user_id in (
	select user_id
	from user_potential_products upp
);