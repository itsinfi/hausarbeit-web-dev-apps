package org.study.iu.jaxrs.interfaces;

public interface MultiThreadingTestable {
    final int DEFAULT_THREADS = 1;
    final String DEFAULT_TASK_THREAD_MODE = System.getenv("THREAD_MODE");
}
