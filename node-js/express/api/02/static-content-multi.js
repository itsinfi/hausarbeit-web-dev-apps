import staticContent from './static-content.js';
import createThreadPool from '../../utils/create-thread-pool.js';

const DEFAULT_THREADS = 1;
const DEFAULT_LENGTH = 1000;

const piscina = createThreadPool('./workers/02.js');

(async () => {
    await Promise.all(Array(piscina.options.minThreads)
        .fill()
        .map(() => piscina.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const threads = Number(req.body.threads) ?? DEFAULT_THREADS;
    const length = Number(req.body.length) ?? DEFAULT_LENGTH;

    if (threads <= 1) {
        return staticContent(req, res);
    }
    
    let promises = [];
    
    for (let i = 0; i < threads; i++) {
        promises.push(piscina.run({
            thread: i,
            threads,
            length,
        }));
    }

    const results = await Promise.all(promises);

    const result = results.join('');

    res.json({
        length,
        result,
    });
}