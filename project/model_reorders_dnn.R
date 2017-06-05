source('project/calc_meanf1score.R')

#'
#'
buildReorderDnn <- function() {
  print('Building model...', quote=F)
  
  reordered.model <<- h2o.deeplearning(
    characteristics
    ,'reordered'
    ,training_frame = train.orders.h2o
    ,validation_frame = validation.orders.h2o
    ,model_id = 'reordered.model'
    ,variable_importances = T
    ,balance_classes = T
    ,score_interval = 45
    ,score_validation_samples=10000
    ,hidden = c(10, 10, 20, 20)
    ,activation="RectifierWithDropout"
    ,nfolds=3
  )
  
  h2o.saveModel(reordered.model, path='C:/Users/sky/Downloads/reordered.model', force = T)
}

#'
#'
makeTrainPredictionsDnn <- function(threshold = .078) {
  print('Making predictions...', quote=F)
  
  predictions <- h2o.predict(
     reordered.model
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