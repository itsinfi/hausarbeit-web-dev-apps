package org.study.iu.httpservlet.classes;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class ThreadMonitor {
    public static void printAllThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);

        System.out.println("=== Currently Active Threads: ===");
        Arrays.stream(threadInfos)
                .filter(info -> info != null)
                .forEach(info -> System.out.printf(
                        """
                        ThreadID: %d\tName: %s\tState: %s%n
                        """,
                        info.getThreadId(),
                        info.getThreadName(),
                        info.getThreadState()
                )
        );
    }
    
    public static void countThreads() {
        long virtualThreadsCount = Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isVirtual)
                .count();
    
        long physicalThreadsCount = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> !t.isVirtual())
                .count();
    
        System.out.println("Virtual Threads: " + virtualThreadsCount);
        System.out.println("Platform (Physical) Threads: " + physicalThreadsCount);
    }    
}
