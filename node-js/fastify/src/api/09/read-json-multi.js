import flattenJsonMulti from '../../utils/09/flatten-json-multi.js';
import createThreadPool from '../../utils/create-thread-pool.js';

const DEFAULT_PARALLELIZATION_THRESHOLD = 3;
const DEFAULT_NESTING_PARALLELIZATION_LIMIT = 3;

const threadPool = createThreadPool('./src/workers/09.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const parallelizationThreshold = Number(req.body.parallelizationThreshold ?? DEFAULT_PARALLELIZATION_THRESHOLD);
    const nestingParallelizationLimit = Number(req.body.nestingParallelizationLimit ?? DEFAULT_NESTING_PARALLELIZATION_LIMIT);

    let numbers = [];

    await flattenJsonMulti(req.body, numbers, 0, parallelizationThreshold, nestingParallelizationLimit, threadPool);

    return {
        parallelizationThreshold,
        nestingParallelizationLimit,
        found: numbers.length,
        result: numbers,
    };
}