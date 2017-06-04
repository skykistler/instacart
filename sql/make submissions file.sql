SELECT order_id
	  ,products = STUFF((
		SELECT ' ' + cast(product_id as varchar(16))
		FROM order_products__pred pred
		WHERE pred.order_id = o.order_id
		FOR XML PATH('')
	  ), 1, 1, '')
FROM orders o
WHERE o.eval_set = 'test'