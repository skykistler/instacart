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
    ,nfolds=2
    ,score_tree_interval = 10
    ,ntrees = 100
    ,max_depth = 9
    ,nbins = 100
    ,stopping_rounds = 3
  )

  h2o.saveModel(reordered.gbm, path='C:/Users/sky/Downloads/reordered.gbm', force = T)
}

#'
#'
makeTrainPredictionsGbm <- function(threshold = .2147) {
  print('Making predictions...', quote=F)
  
  predictions <- h2o.predict(
     h2o.getModel('reordered.gbm')
    ,test.orders.h2o
  )
  
  test.orders$prediction <- ifelse(predictions$p1 >= threshold, 1, 0) %>% as.vector('numeric') %>% factor
  
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