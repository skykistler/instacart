
SELECT_BASKETS <- "
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

join users u
  on u.user_id = o.user_id

where support >= 1000
and u.department_cluster = '%DC%'

order by user_id, order_number, p.support desc;"

#############################################################################################

department_clusters <- db %>% sqlQuery("SELECT distinct department_cluster FROM users")

sapply(department_clusters$department_cluster, function (cluster) {
  cluster_SELECT_BASKETS <- SELECT_BASKETS %>% str_replace('%DC%', cluster)
  
  baskets <- db %>% sqlQuery(cluster_SELECT_BASKETS, rows_at_time = 1024) 
  
  baskets %>%
    write.csv(
      paste('data/facts/baskets/', cluster, '.csv', sep=''),
      quote=F,
      na="NULL",
      row.names=F
    )
  
  print(paste("Wrote out", nrow(baskets), "records for cluster:", cluster))
})