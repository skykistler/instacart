-- For each anti-association, create normalized adjusted suppport variable to signal
-- how strong the relative signal is to product_2

UPDATE products_anti_assc_2
SET adj_support = b.adj_support

FROM (
	SELECT anti_p1.product_id_1
		  ,anti_p1.product_id_2
		  ,adj_support = anti_p1.support / max_support
	FROM products_anti_assc_2 anti_p1

	-- cross reference the maximum support against product_2
	CROSS APPLY (
		SELECT max_support = cast(max(anti_p2.support) as real)
		FROM products_anti_assc_2 anti_p2
		WHERE anti_p2.product_id_2 = anti_p1.product_id_2
	) AS anti_p2

) as b

WHERE b.product_id_1 = products_anti_assc_2.product_id_1 AND b.product_id_2 = products_anti_assc_2.product_id_2