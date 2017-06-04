IF Object_ID('submissions') IS NOT NULL
    DROP VIEW submissions
GO

CREATE VIEW submissions AS

SELECT order_id
	  ,products = COALESCE(
		  STUFF((
			SELECT --TOP (cast(u.mean_order_size as int))
				' ' + cast(product_id as varchar(16))
			FROM order_products__pred pred
			WHERE pred.order_id = o.order_id
			FOR XML PATH('')
		  ), 1, 1, '')
		 ,'None'
	  )
FROM orders o
JOIN users u
	ON u.user_id = o.user_id
WHERE o.eval_set = 'test';
