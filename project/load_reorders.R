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


#'Splits the reorder data into training/validation/testing sets and calls uploadReorders()
#'
#'@param sample.size Sets the total cut of reorder data to train/validate/test with
#'@param train.split Sets the sub-cut of the sample data to use for training/validation
splitReorders <- function(sample.size = .6, train.split = .4) {
  print('Splitting orders...', quote=F)
  
  # Select samples by order ID to keep full orders together
  orders <- user_products_train$order_id %>% factor %>% levels
  
  sample.cut <- sample.split(orders, sample.size)
  
  # Cut off sample.size orders
  orders <- orders[ sample.cut ]
  
  # Split train data
  order.train.sample <- sample.split(orders, train.split)
  
  # Order-grouped training data
  train.orders <<- 
    user_products_train %>% 
    filter(order_id %in% orders[order.train.sample])
  
  # Non-training orders
  non.train.orders <- orders[!order.train.sample]
  
  # Split non-training orders into validation and test sets
  order.validation.sample <- sample.split(non.train.orders, train.split)
  
  # Training-sized set goes to model validation
  validation.orders <<- user_products_train %>% filter(order_id %in% non.train.orders[ order.validation.sample])
  
  # Leave the rest to testing
  test.orders       <<- user_products_train %>% filter(order_id %in% non.train.orders[!order.validation.sample])
  
  # Split off the non-sample orders
  super.orders      <<- user_products_train %>% filter(!(order_id %in% orders))
  
  uploadReorders()
}

#'
#'
uploadReorders <- function() {
  print('Uploading to h2o...', quote=F)
  
  h2o.rm(c('train.orders', 'validation.orders', 'test.orders', 'super.orders'))
  
  train.orders.h2o      <<- as.h2o(train.orders)
  validation.orders.h2o <<- as.h2o(validation.orders)
  test.orders.h2o       <<- as.h2o(test.orders)
  super.orders.h2o      <<- as.h2o(super.orders)
  
  rm(user_products_train, envir = .GlobalEnv)
  gc()
}

##########################################################################################################
