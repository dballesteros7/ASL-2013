EXPLAIN ANALYZE SELECT message.id AS msg_id, queue_id, context, prio, create_time, message, sc.username AS sender, rc.username AS receiver
FROM msg_queue_assoc
INNER JOIN message
    ON msg_queue_assoc.message_id = message.id
INNER JOIN client sc 
    ON sc.id = sender
LEFT OUTER JOIN client rc 
    ON rc.id = receiver
WHERE msg_queue_assoc.queue_id = 1 AND 
      (receiver = 11 OR receiver IS NULL)
ORDER BY prio DESC, create_time DESC
    FETCH FIRST ROW ONLY
