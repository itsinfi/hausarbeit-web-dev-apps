const multiplication = './multiplication.js';
import createThreadPool from '../../utils/create-thread-pool.js';

const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 1;
const DEFAULT_UPPER_BOUND = 2;

const threadPool = createThreadPool('./src/workers/04.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (req) => {
    const threads = Number(req.body.threads ?? 1);
    const iterations = Number(req.body.iterations ?? DEFAULT_ITERATIONS);
    const lowerBound = Number(req.body.lowerBound ?? DEFAULT_LOWER_BOUND);
    const upperBound = Number(req.body.upperBound ?? DEFAULT_UPPER_BOUND);

    if (threads <= 1) {
        return addition(req);
    }

    let product = 1;

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

    results.forEach(r => product *= r);

    return { 
        threads,
        iterations,
        lowerBound,
        upperBound,
        result: product,
    };
}