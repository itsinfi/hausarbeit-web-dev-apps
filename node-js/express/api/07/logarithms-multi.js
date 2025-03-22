const logarithms = './logarithms.js';
import createThreadPool from '../../utils/create-thread-pool.js';

const DEFAULT_ITERATIONS = 1000;

const piscina = createThreadPool('./workers/07.js');

(async () => {
    await Promise.all(Array(piscina.options.minThreads)
        .fill()
        .map(() => piscina.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const threads = Number(req.body.threads) ?? DEFAULT_THREADS;
    const iterations = Number(req.body.iterations) ?? DEFAULT_ITERATIONS;

    let finiteCount = 0;

    let promises = [];

    for (let i = 0; i < threads; i++) {
        promises.push(piscina.run({
            thread: i,
            threads,
            iterations,
        }));
    }

    const results = await Promise.all(promises);

    results.forEach(r => {
        finiteCount += r;
    });

    res.json({ 
        threads,
        iterations,
        result: finiteCount,
    });
}