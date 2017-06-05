#'
#'
loadTestOrders <- function() {
  print('Loading test orders...', quote=F)
  user_products_test <<- db %>% sqlQuery('SELECT * FROM reorder_testing', rows_at_time = 1024)
}

#'
#'
uploadTestOrders <- function() {
  print('Uploading to h2o...', quote=F)
  user_products_test.h2o <<- as.h2o(user_products_test)
}

#'
#'
makeTestPredictions <- function(model_id = 'reordered.gbm', threshold = .235277) {
  print('Making predictions...', quote=F)
  
  test.predictions <<- h2o.predict(
     h2o.getModel(model_id)
    ,user_products_test.h2o
  )
  
  user_products_test$prediction <- ifelse(test.predictions$p1 >= threshold, 1, 0) %>% as.vector('numeric') %>% factor
  
  user_products_test <<- user_products_test
  
  ##################################################################
  print('Inserting predictions...', quote=F)
  
  order_products__pred <- user_products_test
  
  order_products__pred %<>% filter(prediction == 1)
  order_products__pred <- order_products__pred[, c('order_id', 'product_id')]
  
  db %>% sqlQuery('DELETE FROM order_products__pred')
  db %>% sqlSave(order_products__pred, append=T, rownames=F)
  
  ##################################################################
  print('Saving submissions...', quote=F)

  db %>% 
    sqlQuery('SELECT * FROM submissions', rows_at_time = 1024) %>%
    write.csv(
      paste('data/submissions/', Sys.time() %>% as.numeric() %>% round(),'.csv', sep=''),
      quote=F,
      row.names=F
    )
}


if (!exists('user_products_test')) {
  loadTestOrders()
}

if (!exists('user_products_test.h2o')) {
  uploadTestOrders()
}

makeTestPredictions()