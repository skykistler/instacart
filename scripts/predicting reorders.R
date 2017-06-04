# source('project/load_data.R')
# 
# 
# up.training.split <- sample.split(user_products__train$reordered, 1/2)
# 
# user_products.train <- as.h2o(user_products__train[ up.training.split, ])
# user_products.test <- user_products__train[!up.training.split, ]
# 
# rm(user_products__train)

##################################################################
print('Building model...', quote=F)

characteristics <- colnames(user_products.test)[-(1:4)]

# DNN
reordered.model <- h2o.deeplearning(
   characteristics
  ,'reordered'
  ,training_frame = user_products.train
  ,model_id = 'reordered.model'
  ,hidden = c(200, 200)
  ,balance_classes = T
  ,nfolds=2
)

# GBM
# reordered.model <- h2o.gbm(
#   characteristics
#   ,'reordered'
#   ,training_frame = as.h2o(user_products.train)
#   ,model_id = 'reordered.model'
#   ,ntrees = 1000
#   ,max_depth = 10
#   ,nfolds=3
# )

##################################################################
print('Making predictions...', quote=F)

user_products.test$prediction <- h2o.predict(
   reordered.model
  ,user_products.test
)$predict %>% as.vector('numeric') %>% factor


##################################################################
print('Reorders:', quote=F)

table(user_products.test$prediction, user_products.test$reordered)

true.positives <- user_products.test %>% filter(prediction == 1, reordered == 1) %>% nrow
pred.positives <- user_products.test %>% filter(prediction == 1) %>% nrow
test.positives <- user_products.test %>% filter(reordered == 1) %>% nrow

precision <- ((true.positives / pred.positives) * 100) %>% round(2)
recall <-  ((true.positives / test.positives) * 100) %>% round(2)

print(paste('Precision: ', precision, '%', sep=''), quote=F)
print(paste('Recall: ', recall, '%', sep=''), quote=F)

##################################################################
# print('Inserting predictions...', quote=F)
# 
# upsert_pred <- user_products
# 
# upsert_pred$prediction <- h2o.predict(
#   reordered.model
#   ,as.h2o(upsert_pred)
# )$predict %>% as.vector('numeric')
# 
# upsert_pred %<>% filter(prediction == 1)
# upsert_pred <- upsert_pred[, c('order_id', 'product_id')]
# 
# sqlSave(db, upsert_pred, rownames=F)
