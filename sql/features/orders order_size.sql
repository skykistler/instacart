UPDATE orders
SET order_size = b.order_size

FROM (

	SELECT order_id
	      ,order_size = count(1)
	FROM order_products__train
	GROUP BY order_id

) as b

WHERE b.order_id = orders.order_id;
---------------------------------------
UPDATE orders
SET order_size = b.order_size

FROM (

	SELECT order_id
	      ,order_size = count(1)
	FROM order_products
	GROUP BY order_id

) as b

WHERE b.order_id = orders.order_id;