UPDATE products_anti_assc_2
SET rank_support = b.rank_support

FROM (
	select anti1.product_id_1
		  ,anti1.product_id_2
		  ,rank_support = (support * adj_support) / (SELECT max(support) FROM products_anti_assc_2 anti2 WHERE anti1.product_id_1 = anti2.product_id_1)
	from products_anti_assc_2 anti1
) as b

WHERE b.product_id_1 = products_anti_assc_2.product_id_1 AND b.product_id_2 = products_anti_assc_2.product_id_2