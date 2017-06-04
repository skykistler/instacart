
DELETE FROM order_products__pred;
INSERT INTO order_products__pred (order_id, product_id)

SELECT order_id, product_id

FROM (

	SELECT o2.order_id
	      ,product_id = assc.product_id_2

	FROM order_products op1

	INNER JOIN orders o1
		ON o1.order_id = op1.order_id

	INNER JOIN orders o2
		ON o2.user_id = o1.user_id
		AND o2.eval_set = 'train'
		AND o1.order_number + 1 = o2.order_number

	LEFT JOIN user_products up
		ON up.user_id = o1.user_id
		AND up.product_id = op1.product_id

	LEFT JOIN users u
		ON u.user_id = up.user_id

	INNER JOIN products_anti_assc_2 assc
		ON assc.product_id_1 = op1.product_id
		AND o2.days_since_prior_order < up.mean_days_btwn
		AND assc.support > 5000
		AND assc.product_id_2 IN (
			SELECT TOP 1 assc2.product_id_2
			FROM products_anti_assc_2 assc2
			WHERE assc.product_id_1 = assc2.product_id_1 
			ORDER BY support DESC
		)



) AS up

WHERE NOT EXISTS (SELECT 1 FROM order_products__pred pred WHERE pred.order_id = up.order_id AND pred.product_id = up.product_id)
GROUP BY order_id, product_id;

select  (select count(1) from order_products__pred pred where exists (select 1 from order_products__train train where train.order_id = pred.order_id and train.product_id = pred.product_id))
	,(select count(1) from order_products__pred pred ) 