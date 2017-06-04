#'
#'
calculateMeanF1Score <- function() {
  print('Calculating Mean F1-Score...', quote=F)
  
  orders_train <- db %>% 
    sqlQuery("
           SELECT order_id, user_id, order_size
           FROM orders 
           WHERE eval_set = 'train'
  ", rows_at_time=1024)
  
  orders_train %<>% filter(order_id %in% non.train.orders[!order.validation.sample])
  
  
  orders_train %<>%
    left_join(
      test.orders %>%
        group_by(order_id) %>%
        filter(prediction == 1, reordered == 1) %>%
        summarise(
          true.positives = n()
        )
      ,by='order_id'
    )
  
  orders_train %<>%
    left_join(
      test.orders %>%
        group_by(order_id) %>%
        filter(prediction == 1) %>%
        summarise(
          pred.positives = n()
        )
      ,by='order_id'
    )
  
  orders_train %<>%
    left_join(
      test.orders %>%
        group_by(order_id) %>%
        filter(reordered == 1) %>%
        summarise(
          test.positives = n()
        )
      ,by='order_id'
    )
  
  orders_train %<>%
    mutate(
      # This doesn't support 'None' + guesses, only assumes 'None' guess if no predictions made
      none_guess = ifelse(pred.positives %>% is.na & test.positives %>% is.na, 1L, 0L),
      precision = coalesce(true.positives, none_guess) / coalesce(pred.positives, 1L),
      recall    = coalesce(true.positives, none_guess) / coalesce(test.positives, 1L),
      f1        = ifelse(precision == 0 & recall == 0, 0, 2 * (precision * recall) / (precision + recall))
    )
  
  print(paste('Mean F1-Score:', mean(orders_train$f1)), quote=F)
}
