const logarithms = './logarithms.js';
import createThreadPool from '../../utils/create-thread-pool.js';
import config from '../../config/config.js';

const DEFAULT_ITERATIONS = 1000;

const threadPool = createThreadPool('./src/workers/07.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const threads = Number(req.body.threads ?? config.THREAD_POOL_SIZE ?? 1);
    const iterations = Number(req.body.iterations ?? DEFAULT_ITERATIONS);

    let finiteCount = 0;

    let promises = [];

    for (let i = 0; i < threads; i++) {
        promises.push(threadPool.run({
            thread: i,
            threads,
            iterations,
        }));
    }

    const results = await Promise.all(promises);

    results.forEach(r => finiteCount += r);

    res.json({ 
        threads,
        iterations,
        result: finiteCount,
    });
}