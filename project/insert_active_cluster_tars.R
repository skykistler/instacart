#'
#'
loadActiveUserClusterTars <- function() {
  print('Loading active user Cluster-TARS...', quote=F)
  
  department_clusters <- db %>% sqlQuery("SELECT DISTINCT department_cluster FROM users")
  
  # Clear out the users_potential_products table
  db %>% sqlQuery('DELETE FROM user_potential_products')
  
  sapply(department_clusters$department_cluster, function (cluster) {
    cluster_filename <- paste('data/processed/active_tars/', cluster, '.csv', sep='')
    
    if (!file.exists(cluster_filename)) {
      print(paste("Skipping cluster", cluster, "due to no TARS"), quote=F)
      return()
    }
    
    # load specific cluster tars
    cluster_tars <- read_csv(cluster_filename)
    
    if (nrow(cluster_tars) == 0) {
      print(paste("Skipping cluster", cluster, "due to no TARS"), quote=F)
      return()
    }
    
    # add department identifier
    cluster_tars$department_cluster <- cluster
    
    # rearrange columns
    cluster_tars <- cluster_tars[c(5,1,2,3,4)]
    
    # insert/append to table
    db %>% sqlSave(cluster_tars, 'user_potential_products', append=T, rownames=F)
    
    print(paste("Wrote out", nrow(cluster_tars), "records for cluster:", cluster), quote=F)
  }) %>% 
    nrow %>%
    return
}
