import { Piscina, FixedQueue } from 'piscina';

export default function createThreadPool(filename) {
    return new Piscina({
        filename,
        minThreads: Number(process.env.OPERATIONAL_THREAD_POOL_SIZE ?? 4),
        maxThreads: Number(process.env.OPERATIONAL_THREAD_POOL_SIZE ?? 4),
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