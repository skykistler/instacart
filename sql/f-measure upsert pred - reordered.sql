SELECT mean_f1_score = AVG(COALESCE(f_measure, 0))
	  ,precision     = AVG(precision)
	  ,recall        = AVG(recall)
	  ,predictions   = (SELECT COUNT(1) FROM upsert_pred)
	  ,out_of        = (SELECT COUNT(1) FROM order_products__train WHERE reordered = 1)

FROM (

SELECT f_measure = (
		CASE WHEN
			precision  = 0
			AND recall = 0
		THEN 0 ELSE
			2 * (precision * recall) / (precision + recall)
		END
	  ),*

FROM (

SELECT o.order_id
	  ,precision = COALESCE(true_positives, 0) / COALESCE(pred_positives, 1)
	  ,recall    = COALESCE(true_positives, 0) / train_positives

FROM orders o

LEFT OUTER JOIN
(
SELECT pred.order_id, true_positives = CAST(COUNT(1) as real)
FROM upsert_pred pred
INNER JOIN order_products__train train
	ON train.order_id = pred.order_id
	AND train.product_id = pred.product_id
	AND train.reordered = 1
GROUP BY pred.order_id
) AS true_positives
ON true_positives.order_id = o.order_id

LEFT OUTER JOIN
(
SELECT pred.order_id, pred_positives = CAST(COUNT(1) as real)
FROM upsert_pred pred
GROUP BY pred.order_id
) AS pred_positives
ON pred_positives.order_id = o.order_id

LEFT OUTER JOIN
(
SELECT train.order_id, train_positives = CAST(COUNT(1) as real)
FROM order_products__train train
WHERE train.reordered = 1
GROUP BY train.order_id
) AS train_positives
ON train_positives.order_id = o.order_id

WHERE o.eval_set = 'train'

) AS precision_recall
) AS f_measure