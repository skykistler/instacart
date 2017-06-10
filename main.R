source('bootstrap.R')

source('project/load_reorders.R')
source('project/model_reorders_gbm.R')
source('project/test_meanf1score.R')
source('project/test_reorders.R')

if (!exists('user_products_train')) {
  loadReorders()
}

if (!exists('train.orders')) {
  
}

buildReorderGbm()
makeTrainPredictionsGbm()