DROP EVENT IF EXISTS event_purge_zipkin_old_data;

DELIMITER $$

CREATE EVENT event_purge_zipkin_old_data
ON SCHEDULE EVERY 1 DAY STARTS CURRENT_TIMESTAMP
DO
BEGIN
  DECLARE delete_before_ts BIGINT;
  
  SET delete_before_ts = UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) * 1000000;

  DELETE FROM zipkin_annotations WHERE a_timestamp < delete_before_ts;
  DELETE FROM zipkin_spans WHERE start_ts < delete_before_ts;
  DELETE FROM zipkin_dependencies WHERE day < DATE_SUB(CURDATE(), INTERVAL 7 DAY);
END $$

DELIMITER ;