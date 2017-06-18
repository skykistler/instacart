#'
#'
loadDepartmentClusterTars <- function() {
  print('Loading department Cluster-TARS...', quote=F)
  
  department_clusters <- db %>% sqlQuery("SELECT DISTINCT department_cluster FROM users")
  
  # Clear out the department TARS table
  db %>% sqlQuery('DELETE FROM cluster_tars_department')
  
  sapply(department_clusters$department_cluster, function (cluster) {
    cluster_filename <- paste('data/processed/tars/', cluster, '.csv', sep='')
    
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
    cluster_tars <- cluster_tars[c(8,1,2,3,4,5,6,7)]
    
    # insert/append to table
    db %>% sqlSave(cluster_tars, 'cluster_tars_department', append=T, rownames=F)
    
    print(paste("Wrote out", nrow(cluster_tars), "records for cluster:", cluster), quote=F)
  })
}
