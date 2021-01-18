package org.ifsoft.websockets;

import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class QueuedThreadPoolProvider {

        private static final int MAX_THREADS = 1024;
        private static final QueuedThreadPool queuedThreadPool;
        
        static {
                queuedThreadPool = new QueuedThreadPool(MAX_THREADS,1);
        }

        public static QueuedThreadPool getQueuedThreadPool(String name) {
                if (! queuedThreadPool.isRunning())
                {
                        queuedThreadPool.setName(name);
                }
                return queuedThreadPool;
        }
}
