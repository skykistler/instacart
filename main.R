source('bootstrap.R')

source('project/load_reorders.R')
source('project/model_reorders_gbm.R')
source('project/test_meanf1score.R')
source('project/test_reorders.R')

# if (!exists('train.orders')) {
#   loadReorders()
#   splitReorders()
# }
# 
# buildReorderGbm()
# makeTrainPredictionsGbm()
# 
# 
# makeTrainPredictionsGbm(orders = super.orders, orders.h2o = super.orders.h2o)
