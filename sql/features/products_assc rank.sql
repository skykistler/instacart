UPDATE products_assc_2
SET rank = b.rank

FROM (
	
	SELECT assc.product_id_1
		  ,assc.product_id_2
		  ,rank = ROW_NUMBER() OVER (PARTITION BY assc.product_id_1 ORDER BY rank_support DESC)
	FROM products_assc_2 assc


) AS b

WHERE b.product_id_1 = products_assc_2.product_id_1 and b.product_id_2 = products_assc_2.product_id_2