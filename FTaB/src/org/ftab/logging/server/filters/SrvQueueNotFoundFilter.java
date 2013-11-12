package org.ftab.logging.server.filters;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.ftab.logging.SystemEvent;
import org.ftab.logging.server.ClientConnectionLogRecord;

public class SrvQueueNotFoundFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record instanceof ClientConnectionLogRecord) {
            ClientConnectionLogRecord cRecord = (ClientConnectionLogRecord)record;
            
            // Narrow down to retrieve that was not successful but not an error
            if (cRecord.getEventCategory() == SystemEvent.FETCH_WAITING_QUEUES && !cRecord.isSuccess()) {
                                
                // Records to be logged were marked with info
                if (cRecord.getLevel() == Level.INFO) {
                    return true;
                }
            }
        }
        
        return false;
    }

}



