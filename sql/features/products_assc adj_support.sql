-- For each anti-association, create normalized adjusted suppport variable to signal
-- how strong the relative signal is to product_2

UPDATE products_assc_2
SET adj_support = b.adj_support

FROM (
	SELECT assc_p1.product_id_1
		  ,assc_p1.product_id_2
		  ,adj_support = assc_p1.support / max_support
	FROM products_anti_assc_2 assc_p1

	-- cross reference the maximum support against product_2
	CROSS APPLY (
		SELECT max_support = cast(max(assc_p2.support) as real)
		FROM products_assc_2 assc_p2
		WHERE assc_p2.product_id_2 = assc_p1.product_id_2
	) AS anti_p2

) as b

WHERE b.product_id_1 = products_assc_2.product_id_1 AND b.product_id_2 = products_assc_2.product_id_2