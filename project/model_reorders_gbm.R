#'
#'
buildReorderGbm <- function() {
  print('Building model...', quote=F)

  reordered.gbm <<- h2o.gbm(
     characteristics
    ,'reordered'
    ,training_frame = train.orders.h2o
    ,validation_frame = validation.orders.h2o
    ,model_id = 'reordered.gbm'
    ,balance_classes = T
    ,nfolds=3
    ,ntrees = 80
    ,max_depth = 6
    ,min_rows = 2
    ,nbins = 32
    ,learn_rate = .2
    ,learn_rate_annealing = .995
    ,score_tree_interval = 7
    ,stopping_rounds = 3
    ,sample_rate = .95
    ,col_sample_rate = .95
  )

  h2o.saveModel(h2o.getModel('reordered.gbm'), path='C:/Users/sky/Downloads/reordered.deep.gbm', force = T)
}

#'
#'
makeTrainPredictionsGbm <- function(
    model_id = 'reordered.gbm', 
    orders = test.orders, 
    orders.h2o = test.orders.h2o
  ) {
  print('Making predictions...', quote=F)
  
  model <- h2o.getModel(model_id)
  
  performance <- h2o.performance(model, validation.orders.h2o)
  threshold   <- performance@metrics$max_criteria_and_metric_scores[1, 'threshold']
  
  predictions <- h2o.predict(
     model
    ,orders.h2o
  )
  
  orders$prediction <- ifelse(predictions$p1 >= threshold, 1, 0) %>% as.vector('numeric') %>% factor
  
  threshold   <<- threshold
  h2o.rm(predictions)
  
  ###########################################
  print('Reorders:', quote=F)
  
  print(table(orders$prediction, orders$reordered))
  
  true.positives <- orders %>% filter(prediction == 1, reordered == 1) %>% nrow
  pred.positives <- orders %>% filter(prediction == 1) %>% nrow
  test.positives <- orders %>% filter(reordered == 1) %>% nrow
  
  precision <- ((true.positives / pred.positives) * 100) %>% round(2)
  recall <-  ((true.positives / test.positives) * 100) %>% round(2)
  
  print(paste('Precision: ', precision, '%', sep=''), quote=F)
  print(paste('Recall: ', recall, '%', sep=''), quote=F)
  
  calculateMeanF1Score(orders)
}