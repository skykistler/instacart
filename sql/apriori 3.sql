INSERT INTO products_assc_3

SELECT product_id_1 = op1.product_id
	  ,product_id_2 = op2.product_id
	  ,product_id_3 = op3.product_id
	  ,support      = count(*)

FROM order_products op1

INNER JOIN order_products op2
	ON op1.order_id = op2.order_id
	AND op1.product_id < op2.product_id

INNER JOIN order_products op3
	ON op1.order_id = op3.order_id
	AND op2.product_id < op3.product_id

GROUP BY op1.product_id, op2.product_id, op3.product_id