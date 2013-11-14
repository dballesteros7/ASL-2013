EXPLAIN ANALYZE WITH temp AS (DELETE FROM msg_queue_assoc 
                WHERE message_id = 576413 AND queue_id = (SELECT id FROM queue WHERE name = 'NotOriginallyNamedQueue1')
                RETURNING queue_id, message_id) 
DELETE FROM message USING temp 
WHERE message.id = temp.message_id AND NOT EXISTS (SELECT 1 FROM msg_queue_assoc msa
                                                   WHERE msa.message_id = temp.message_id AND 
                                                   msa.queue_id <> temp.queue_id)

