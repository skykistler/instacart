source('main.R')


# if (!exists('aisles')) {
#   aisles         <- db %>% sqlQuery('SELECT * FROM aisles')
# }
# 
# 
# if (!exists('departments')) {
#   departments    <- db %>% sqlQuery('SELECT * FROM departments')
# }
# 
# 
# if (!exists('products')) {
#   products       <- db %>% sqlQuery('SELECT * FROM products')
# }
# 
# 
# if (!exists('orders')) {
#   orders         <- db %>% sqlQuery('SELECT * FROM orders')
# }
#   

# if (!exists('order_products__train')) {
#   order_products__train <- db %>% sqlQuery('SELECT * FROM order_products__train')
# }

# if (!exists('user_products__train')) {
#   print('Loading user_products...', quote=F)
#   user_products__train <- db %>% sqlQuery('SELECT * FROM user_products__train')
# 
#   user_products__train$reordered %<>% factor
# }

# if (!exists('user_products__test')) {
#   print('Loading user_products test set...', quote=F)
#   user_products__test <- db %>% sqlQuery('SELECT * FROM user_products__test')
#   
#   user_products__test$reordered %<>% factor
# }