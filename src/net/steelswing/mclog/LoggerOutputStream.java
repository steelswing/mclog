/*
 * Ну вы же понимаете, что код здесь только мой?
 * Well, you do understand that the code here is only mine?
 */

package net.steelswing.mclog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MrJavaCoder
 */
public class LoggerOutputStream extends ByteArrayOutputStream {

    private final String separator = System.getProperty("line.separator");
    private final Logger logger;
    private final Level level;

    public LoggerOutputStream(Logger logger, Level level) {
        super();
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            super.flush();
            String record = this.toString();
            super.reset();

            if ((record.length() > 0) && (!record.equals(separator))) {
                logger.log(level, record);
            }
        }
    }
}
