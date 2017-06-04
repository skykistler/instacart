####### Required libraries for scripts ####### 

.requiredLibraries <- c(
  'dplyr',
  'magrittr',
  'stringr',
  'RODBC',
  'stringdist',
  'e1071',
  'randomForest',
  'caTools',
  'readr',
  'RcppRoll',
  'git2r',
  'ggplot2',
  'h2o'
)

####################################################################
## All libraries will be installed if needed

.requireLib <- function(package) {
  if (!require(package, character.only=T)) {
    install.packages(package)
    return(require(package, character.only=T))
  }
  
  return(TRUE)
}

lapply(.requiredLibraries, .requireLib)