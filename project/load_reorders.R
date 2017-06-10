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
splitReorders <- function(sample.size = .4, train.split = .1) {
  print('Splitting orders...', quote=F)
  
  # Split with order ID's to keep orders together
  orders <<- user_products_train$order_id %>% factor %>% levels
  order.model.sample <<- sample.split(orders, sample.size)
  
  # Sample entire set to reduce data
  orders <<- 
    user_products_train %>% 
    filter(order_id %in% orders[order.model.sample])
  
  ### Split train/validation/test data
  
  order.train.sample <<- sample.split(orders, train.split)
  
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
  order.validation.sample <<- sample.split(non.train.orders, train.split)
  
  # Smaller set goes to model validation
  validation.orders <<- user_products_train %>% filter(order_id %in% non.train.orders[ order.validation.sample])
  
  # Leave the rest to testing
  test.orders       <<- user_products_train %>% filter(order_id %in% non.train.orders[!order.validation.sample])
  
  uploadReorders()
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
