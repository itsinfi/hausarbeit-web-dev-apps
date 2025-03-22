const powers = './powers.js';
import createThreadPool from '../../utils/create-thread-pool.js';

const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 1;
const DEFAULT_UPPER_BOUND = 2;

const piscina = createThreadPool('./workers/06.js');

(async () => {
    await Promise.all(Array(piscina.options.minThreads)
        .fill()
        .map(() => piscina.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const threads = Number(req.body.threads) ?? DEFAULT_THREADS;
    const iterations = Number(req.body.iterations) ?? DEFAULT_ITERATIONS;
    const lowerBound = Number(req.body.lowerBound) ?? DEFAULT_LOWER_BOUND;
    const upperBound = Number(req.body.upperBound) ?? DEFAULT_UPPER_BOUND;

    if (threads <= 1) {
        return addition(req, res);
    }

    let sum = 0;

    let promises = [];

    for (let i = 0; i < threads; i++) {
        promises.push(piscina.run({
            thread: i,
            threads,
            iterations,
            upperBound,
            lowerBound,
        }));
    }

    const results = await Promise.all(promises);

    results.forEach(r => {
        sum += r;
    });

    res.json({ 
        threads,
        iterations,
        lowerBound,
        upperBound,
        result: sum,
    });
}