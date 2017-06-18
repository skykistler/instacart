UPDATE
	frequent_departments
SET 
	department_cluster = CONCAT(department_id_1, ' ', department_id_2, ' ', department_id_3);

ALTER TABLE frequent_departments ALTER COLUMN department_cluster varchar(8) NOT NULL;

ALTER TABLE users ALTER COLUMN department_cluster varchar(8) NOT NULL;
ALTER TABLE cluster_tars_department ALTER COLUMN department_cluster varchar(8) NOT NULL;

INSERT INTO frequent_departments (support, department_id_1, department_cluster)

SELECT support = count(1), department_id_1 = cast(department_cluster as int), department_cluster
FROM [instacart].[dbo].[users]

where not exists (select 1 from frequent_departments where department_cluster = users.department_cluster)
group by department_cluster;