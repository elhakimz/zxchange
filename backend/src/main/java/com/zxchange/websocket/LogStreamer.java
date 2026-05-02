package com.zxchange.websocket;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogStreamer {

    private final StompBroadcaster broadcaster;

    public LogStreamer(StompBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @PostConstruct
    public void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        WebSocketLogAppender appender = new WebSocketLogAppender(broadcaster);
        appender.setContext(loggerContext);
        appender.setName("WEBSOCKET_LOGS");
        appender.start();

        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }

    private static class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {
        private final StompBroadcaster broadcaster;

        public WebSocketLogAppender(StompBroadcaster broadcaster) {
            this.broadcaster = broadcaster;
        }

        @Override
        protected void append(ILoggingEvent event) {
            // Filter by level first
            if (event.getLevel().toInt() < Level.INFO_INT) {
                return;
            }

            // Avoid infinite loops if STOMP itself logs something
            if (event.getLoggerName().contains("org.springframework.messaging") || 
                event.getLoggerName().contains("com.zxchange.websocket.StompBroadcaster")) {
                return;
            }

            String levelColor = getLevelColor(event.getLevel());
            String message = String.format("[%s] %-15s - %s", 
                event.getLevel(), 
                truncateLogger(event.getLoggerName()), 
                event.getFormattedMessage());
            
            broadcaster.broadcastLog(message);
        }

        private String truncateLogger(String name) {
            int lastDot = name.lastIndexOf('.');
            return lastDot > 0 ? name.substring(lastDot + 1) : name;
        }

        private String getLevelColor(Level level) {
            // We'll handle coloring on the frontend
            return level.toString();
        }
    }
}
