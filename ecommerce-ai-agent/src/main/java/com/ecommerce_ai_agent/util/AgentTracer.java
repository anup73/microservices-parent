package com.ecommerce_ai_agent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AgentTracer {
    private static final String TRACE_LOG_FILE = "agent_trace.log";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public void trace(String step, String detail) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [STEP: %s] %s", timestamp, step, detail);
        
        System.out.println("TRACE: " + logEntry); // Also print to console for immediate visibility
        
        try (FileWriter fw = new FileWriter(TRACE_LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to trace log: " + e.getMessage());
        }
    }
}
