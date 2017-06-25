#'
#'
loadActiveUserTars <- function() {
  print('Loading active user TARS...', quote=F)
  
  user_ids <- db %>% sqlQuery("SELECT DISTINCT user_id FROM users")
  
  # Clear out the users_potential_products table
  db %>% sqlQuery('DELETE FROM user_potential_products')
  
  sapply(user_ids$user_id, function (user_id) {
    user_filename <- paste('data/processed/active_tars_user/', user_id, '.csv', sep='')
    
    if (!file.exists(user_filename)) {
      print(paste("Skipping user", user_id, "due to no TARS"), quote=F)
      return()
    }
    
    # load specific user tars
    user_tars <- read_csv(user_filename)
    
    if (nrow(user_tars) == 0) {
      print(paste("Skipping user", user_id, "due to no TARS"), quote=F)
      return()
    }
    
    # insert/append to table
    db %>% sqlSave(user_tars, 'user_potential_products', append=T, rownames=F)
    
  })
  
  
  # rearrange columns
}
