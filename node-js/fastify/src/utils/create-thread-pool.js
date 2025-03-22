import { Piscina, FixedQueue } from 'piscina';
import config from '../config/config.js';

export default function createThreadPool(filename) {
    return new Piscina({
        filename,
        minThreads: config.THREAD_POOL_SIZE ?? 10,
        maxThreads: config.THREAD_POOL_SIZE ?? 10,
        concurrentTasksPerWorker: 1,
        taskQueue: new FixedQueue(),
        maxQueue: 'auto',
        idleTimeout: 60_000,
        niceIncrement: 20,
        resourceLimits: {
            maxOldGenerationSizeMb: 2048,
            maxYoungGenerationSizeMb: 256,
        },
    });
}