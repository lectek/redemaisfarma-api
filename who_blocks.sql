SHOW FULL PROCESSLIST;
SHOW ENGINE INNODB STATUS\G
SELECT trx_id, trx_mysql_thread_id, trx_started, trx_query
FROM information_schema.innodb_trx;
