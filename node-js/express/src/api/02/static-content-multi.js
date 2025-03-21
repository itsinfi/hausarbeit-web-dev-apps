import staticContent from './static-content.js';
import createThreadPool from '../../utils/create-thread-pool.js';
import config from '../../config/config.js';

const DEFAULT_LENGTH = 1000;

const threadPool = createThreadPool('./src/workers/02.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const threads = Number(req.body.threads ?? config.THREAD_POOL_SIZE ?? 1);
    const length = Number(req.body.length ?? DEFAULT_LENGTH);

    if (threads <= 1) {
        return staticContent(req, res);
    }
    
    let promises = [];
    
    for (let i = 0; i < threads; i++) {
        promises.push(threadPool.run({
            thread: i,
            threads,
            length,
        }));
    }

    const results = await Promise.all(promises);

    const result = results.join('');

    res.json({
        threads,
        length,
        result,
    });
}