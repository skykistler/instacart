#'
#'
loadDepartmentTarSequences <- function() {
  print('Loading department Cluster-TARS...', quote=F)
  
  department_clusters <- db %>% sqlQuery("SELECT DISTINCT department_cluster FROM users")
  
  # Clear out the department TARS table
  db %>% sqlQuery('DELETE FROM cluster_tars_department')
  
  sapply(department_clusters$department_cluster, function (cluster) {
    # load specific cluster tars
    cluster_tars <- read_csv(paste('data/processed/tars/', cluster, '.csv', sep=''))
    
    # add department identifier
    cluster_tars$department_cluster <- cluster
    
    # rearrange columns
    cluster_tars <- cluster_tars[c(8,1,2,3,4,5,6,7)]
    
    # insert/append to table
    db %>% sqlSave(cluster_tars, 'cluster_tars_department', append=T, rownames=F)
    
    print(paste("Wrote out", nrow(cluster_tars), "records for cluster:", cluster))
  })
}
