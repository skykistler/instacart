SELECT *

FROM user_products up
INNER JOIN orders o1
	ON o1.user_id = up.user_id
	AND o1.eval_set = 'train'

INNER JOIN order_products__train op1
	ON op1.order_id = o1.order_id
	AND op1.product_id = (
		SELECT TOP 1 product_id_2
		FROM products_anti_assc_2 assc
		WHERE assc.product_id_1 = up.product_id
		ORDER BY support DESC
	)
	--AND EXISTS ( -- Where product was ordered again anyway (assc not anti_assc)
	AND NOT EXISTS ( -- Where product was possibly ordered as a replacement (anti assc)
		SELECT 1
		FROM order_products__train op2
		WHERE op2.order_id = op1.order_id 
		AND op2.product_id = up.product_id
	)

WHERE orders_since_last = 0 AND times_ordered > 3