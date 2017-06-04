INSERT INTO products_assc_2 (product_id_1, product_id_2, support)

SELECT product_id_1 = op1.product_id
	  ,product_id_2 = op2.product_id
	  ,support = count(1)

FROM order_products op1

LEFT JOIN orders o1
	ON o1.order_id = op1.order_id

INNER JOIN orders o2
	ON o2.user_id = o1.user_id
	AND o2.order_number = o1.order_number + 1
	AND o2.eval_set = 'prior'

INNER JOIN order_products op2
	ON op2.order_id = o2.order_id
	AND op2.product_id != op1.product_id
	AND EXISTS (
		SELECT 1
		FROM order_products op3
		WHERE op3.order_id = op2.order_id
		AND op3.product_id = op1.product_id	
	)

GROUP BY op1.product_id, op2.product_id

HAVING count(1) > 1;