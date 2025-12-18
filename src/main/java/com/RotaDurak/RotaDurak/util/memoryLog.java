package com.RotaDurak.RotaDurak.util;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class memoryLog {
    @Scheduled(fixedRate = 5000)
    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();

        System.out.println(String.format(
                "🧠 Memory | Used: %d MB | Total: %d MB | Max: %d MB",
                usedMemory / 1024 / 1024,
                totalMemory / 1024 / 1024,
                maxMemory / 1024 / 1024
        ));
    }
}
