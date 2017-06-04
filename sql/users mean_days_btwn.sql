update users
set mean_days_btwn = b.mean_days_btwn

from (

select user_id
      ,mean_days_btwn = avg(days_since_prior_order) 

from orders o 
where days_since_prior_order is not null

group by user_id

) as b

where users.user_id = b.user_id