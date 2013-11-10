EXPLAIN ANALYZE SELECT message.id AS msg_id, queue.id AS queue_id, context, prio, create_time, message, queue.name, sc.username AS sender, rc.username AS receiver
FROM message 
INNER JOIN msg_queue_assoc
    ON msg_queue_assoc.message_id = message.id
INNER JOIN queue 
    ON msg_queue_assoc.queue_id = queue.id
INNER JOIN client sc 
    ON sc.id = sender
LEFT JOIN client rc 
    ON rc.id = receiver
WHERE queue.name = 'NotOriginallyNamedQueue1' AND 
      (receiver = 11 OR receiver IS NULL)
ORDER BY prio DESC, create_time DESC
    FETCH FIRST ROW ONLY
