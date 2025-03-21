const { Worker } = 'worker_threads';
const addition = './addition.js';

const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 0;
const DEFAULT_UPPER_BOUND = 1;

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
        promises.push(new Promise((resolve, reject) => {
            const worker = new Worker('./workers/03.js');
            worker.postMessage({ thread: i, threads, iterations, upperBound, lowerBound });
            worker.on('message', resolve);
            worker.on('error', reject);
        }));
    }

    const results = await Promise.all(promises);

    results.forEach(r => {
        sum += r;
    });

    res.json({ 
        iterations,
        lowerBound,
        upperBound,
        result: sum,
    });
}