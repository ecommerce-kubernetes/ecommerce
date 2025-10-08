CREATE OR REPLACE VIEW vw_product_summary AS
SELECT
  p.id,
  p.name,
  p.description,
  p.category_id,
  p.create_at,
  bi.image_url as thumbnail,
  bv.price as minimum_price,
  (bv.price * (100 - COALESCE(bv.discount_value, 0)) / 100.0) AS discounted_price,
  bv.discount_value as discount_rate,
  COALESCE(ra.avg_rating, 0.0)  AS avg_rating,
  COALESCE(ra.review_count, 0)  AS review_count
FROM product p
LEFT JOIN category c
  ON c.id = p.category_id
LEFT JOIN (
  /* best variant per product : derived table using ROW_NUMBER() */
  SELECT t.product_id, t.id, t.price, t.discount_value
  FROM (
    SELECT pv.product_id,
           pv.id,
           pv.price,
           pv.discount_value,
           ROW_NUMBER() OVER (
             PARTITION BY pv.product_id
             ORDER BY (pv.price * (100 - COALESCE(pv.discount_value,0)) / 100.0) ASC,
                      pv.price ASC,
                      pv.id ASC
           ) AS rn
    FROM product_variant pv
  ) AS t
  WHERE t.rn = 1
) AS bv
  ON bv.product_id = p.id
LEFT JOIN (
  /* best image per product : derived table using ROW_NUMBER() */
  SELECT t2.product_id, t2.image_url
  FROM (
    SELECT im.product_id, im.image_url,
           ROW_NUMBER() OVER (PARTITION BY im.product_id
                              ORDER BY im.sort_order ASC, im.id ASC) AS rn
    FROM product_image im
  ) AS t2
  WHERE t2.rn = 1
) AS bi
  ON bi.product_id = p.id
LEFT JOIN (
  /* review aggregated per product (join via product_variant) */
  SELECT pv.product_id,
         AVG(r.rating)    AS avg_rating,
         COUNT(*)         AS review_count
  FROM review r
  JOIN product_variant pv
    ON r.product_variant_id = pv.id
  GROUP BY pv.product_id
) AS ra
  ON ra.product_id = p.id;