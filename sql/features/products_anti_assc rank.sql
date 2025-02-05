UPDATE products_anti_assc_2
SET rank = b.rank

FROM (
	
	SELECT anti_assc.product_id_1
		  ,anti_assc.product_id_2
		  ,rank = ROW_NUMBER() OVER (PARTITION BY anti_assc.product_id_1 ORDER BY rank_support DESC)
	FROM products_anti_assc_2 anti_assc


) AS b

WHERE b.product_id_1 = products_anti_assc_2.product_id_1 and b.product_id_2 = products_anti_assc_2.product_id_2