source('project/calc_meanf1score.R')

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
    ,nfolds=6
    ,ntrees = 500
    ,max_depth = 20
    ,min_rows = 3
    ,nbins = 20
    ,learn_rate = .08
    ,learn_rate_annealing = .99
    ,score_tree_interval = 15
    ,stopping_rounds = 5
    ,sample_rate = .8
    ,col_sample_rate = .8
  )

  h2o.saveModel(h2o.getModel('reordered.gbm'), path='C:/Users/sky/Downloads/reordered.deep.gbm', force = T)
}

#'
#'
makeTrainPredictionsGbm <- function() {
  print('Making predictions...', quote=F)
  
  model <- h2o.getModel('reordered.gbm')
  
  performance <- h2o.performance(model, validation.orders.h2o)
  threshold   <- performance@metrics$max_criteria_and_metric_scores[1, 'threshold']
  
  predictions <- h2o.predict(
     model
    ,test.orders.h2o
  )
  
  test.orders$prediction <- ifelse(predictions$p1 >= threshold, 1, 0) %>% as.vector('numeric') %>% factor
  
  threshold   <<- threshold
  predictions <<- predictions
  test.orders <<- test.orders
  
  ###########################################
  print('Reorders:', quote=F)
  
  print(table(test.orders$prediction, test.orders$reordered))
  
  true.positives <- test.orders %>% filter(prediction == 1, reordered == 1) %>% nrow
  pred.positives <- test.orders %>% filter(prediction == 1) %>% nrow
  test.positives <- test.orders %>% filter(reordered == 1) %>% nrow
  
  precision <- ((true.positives / pred.positives) * 100) %>% round(2)
  recall <-  ((true.positives / test.positives) * 100) %>% round(2)
  
  print(paste('Precision: ', precision, '%', sep=''), quote=F)
  print(paste('Recall: ', recall, '%', sep=''), quote=F)
  
  calculateMeanF1Score()
}