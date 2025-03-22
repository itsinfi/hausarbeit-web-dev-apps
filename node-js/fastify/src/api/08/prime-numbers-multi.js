const primeNumbers = './prime-numbers.js';
import createThreadPool from '../../utils/create-thread-pool.js';
import config from '../../config/config.js';

const DEFAULT_AMOUNT = 1000;

const threadPool = createThreadPool('./src/workers/08.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (request, reply) => {
    const threads = Number(request.body.threads ?? config.THREAD_POOL_SIZE ?? 1);
    const amount = Number(request.body.amount ?? DEFAULT_AMOUNT);

    if (threads <= 1) {
        return primeNumbers(request, reply);
    }

    let primes = [];
    let limit = amount;
    let iterations = 0;

    do {
        const squareRootOfLimit = Math.sqrt(limit);

        primes.length = 0;

        const sieve = Array.from(
            { length: limit + 1 },
            (_, i) => (i > 1)
        );

        for (let i = 2; i <= squareRootOfLimit; i++) {
            if (sieve[i]) {
                for (let j = i * i; j <= limit; j += i) {
                    sieve[j] = false;
                }
            }
        }

        let promises = Array.from(
            { length: threads },
            (_, threadIndex) => threadPool.run({
                threadIndex,
                threads,
                limit,
                squareRootOfLimit,
                sieve,
            })
        );

        await Promise.all(promises);

        for (let i = 2; i <= limit; i++) {
            if (sieve[i]) {
                primes.push(i);
            }
        }

        limit *= 2;
        iterations++;
    } while (primes.length < amount);

    const result = primes.slice(0, amount);

    reply.send({
        threads,
        iterations,
        found: result.length,
        result,
    });
}