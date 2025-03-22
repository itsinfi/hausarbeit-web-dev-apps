const division = './division.js';
import createThreadPool from '../../utils/create-thread-pool.js';
import config from '../../config/config.js';

const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 1;
const DEFAULT_UPPER_BOUND = 2;

const threadPool = createThreadPool('./src/workers/05.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const threads = Number(req.body.threads ?? config.THREAD_POOL_SIZE ?? 1);
    const iterations = Number(req.body.iterations ?? DEFAULT_ITERATIONS);
    const lowerBound = Number(req.body.lowerBound ?? DEFAULT_LOWER_BOUND);
    const upperBound = Number(req.body.upperBound ?? DEFAULT_UPPER_BOUND);

    let quotient = Number.MAX_VALUE;

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

    results
        .sort((a, b) => a.thread > b.thread)
        .forEach(r => quotient /= r.quotient);

    res.json({ 
        threads,
        iterations,
        lowerBound,
        upperBound,
        result: quotient,
    });
}