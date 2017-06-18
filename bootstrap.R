source('libraries.R')

#### Make database connection ####
try ({
  odbcGetInfo(db)
}, {
  db <<- odbcDriverConnect('driver={SQL Server};server=SKY-PC\\SQLEXPRESS;database=instacart;trusted_connection=true', rows_at_time = 1024)
})

#### Connect to h2o ####
h2o.init(nthreads=3, max_mem_size='8G')
