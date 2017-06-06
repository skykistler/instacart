source('bootstrap.R')

#'
#'
loadReorders <- function() {
  print('Loading orders...', quote=F)
  
  user_products_train <- db %>% sqlQuery('SELECT * FROM reorder_training', rows_at_time = 1024)
  user_products_train$reordered %<>% factor
  
  user_products_train <<- user_products_train
  characteristics <<- colnames(user_products_train)[-(1:4)]
  
  rm(train.orders, envir = .GlobalEnv)
}


#'
#'
splitReorders <- function(ratios = 1/4) {
  print('Splitting orders...', quote=F)
  
  # Split with order ID's to keep orders together
  orders <<- user_products_train$order_id %>% factor %>% levels
  order.train.sample <<- sample.split(orders, ratios)
  
  # Order-grouped training data
  train.orders <<- 
    user_products_train %>% 
    filter(order_id %in% orders[order.train.sample])
  
  # Non-training orders
  non.train.orders <<- 
    (user_products_train %>% 
        filter(order_id %in% orders[!order.train.sample])
    )$order_id %>% 
    factor %>% levels
  
  # Split non-training orders
  order.validation.sample <<- sample.split(non.train.orders, 1 / ((1/ratios) - 1))
  
  # Smaller set goes to model validation
  validation.orders <<- user_products_train %>% filter(order_id %in% non.train.orders[ order.validation.sample])
  
  # Leave the rest to testing
  test.orders       <<- user_products_train %>% filter(order_id %in% non.train.orders[!order.validation.sample])
  
  rm(train.orders.h2o, envir = .GlobalEnv)
}

#'
#'
uploadReorders <- function() {
  print('Uploading to h2o...', quote=F)
  
  h2o.rm(c('train.orders', 'validation.orders', 'test.orders'))
  
  train.orders.h2o      <<- as.h2o(train.orders)
  validation.orders.h2o <<- as.h2o(validation.orders)
  test.orders.h2o       <<- as.h2o(test.orders)
}

##########################################################################################################

# if (!exists('user_products_train')) {
#   loadReorders()
# }
# 
# if (!exists('train.orders')) {
#   splitReorders()
# }
# 
# if (!exists('train.orders.h2o')) {
#   uploadReorders()
#   rm(user_products_train)
# }