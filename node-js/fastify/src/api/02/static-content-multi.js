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

export default async (request, reply) => {
    const threads = Number(request.body.threads ?? config.THREAD_POOL_SIZE ?? 1);
    const length = Number(request.body.length ?? DEFAULT_LENGTH);

    if (threads <= 1) {
        return staticContent(request, reply);
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

    reply.send({
        threads,
        length,
        result,
    });
}