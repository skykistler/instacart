UPDATE products_assc_2
SET rank_support = b.rank_support

FROM (
	select assc1.product_id_1
		  ,assc1.product_id_2
		  ,rank_support = (support * adj_support) / (SELECT max(support) FROM products_assc_2 assc2 WHERE assc1.product_id_1 = assc2.product_id_1)
	from products_assc_2 assc1
) as b

WHERE b.product_id_1 = products_assc_2.product_id_1 AND b.product_id_2 = products_assc_2.product_id_2