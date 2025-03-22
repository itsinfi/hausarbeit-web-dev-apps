const addition = './addition.js';
import createThreadPool from '../../utils/create-thread-pool.js';
import config from '../../config/config.js';

const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 0;
const DEFAULT_UPPER_BOUND = 1;

const threadPool = createThreadPool('./src/workers/03.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (request, reply) => {
    const threads = Number(request.body.threads ?? config.THREAD_POOL_SIZE ?? 1);
    const iterations = Number(request.body.iterations ?? DEFAULT_ITERATIONS);
    const lowerBound = Number(request.body.lowerBound ?? DEFAULT_LOWER_BOUND);
    const upperBound = Number(request.body.upperBound ?? DEFAULT_UPPER_BOUND);

    if (threads <= 1) {
        return addition(request, reply);
    }

    let sum = 0;

    let promises = [];

    for (let i = 0; i < threads; i++) {
        promises.push(threadPool.run({
            thread: i,
            threads,
            iterations,
            upperBound,
            lowerBound,
        }));
    }

    const results = await Promise.all(promises);

    results.forEach(r => sum += r);

    reply.send({ 
        threads,
        iterations,
        lowerBound,
        upperBound,
        result: sum,
    });
}