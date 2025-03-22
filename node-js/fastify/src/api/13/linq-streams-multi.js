import flattenJsonMulti from '../../utils/13/flatten-json-multi.js';
import createThreadPool from '../../utils/create-thread-pool.js';
import parallelSortArray from '../../utils/parallel-sort-array.js';
import config from '../../config/config.js';

const DEFAULT_PARALLELIZATION_THRESHOLD = 3;
const DEFAULT_NESTING_PARALLELIZATION_LIMIT = 3;

const threadPool = createThreadPool('./src/workers/13.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (request, reply) => {
    const parallelizationThreshold = Number(request.body.parallelizationThreshold ?? DEFAULT_PARALLELIZATION_THRESHOLD);
    const nestingParallelizationLimit = Number(request.body.nestingParallelizationLimit ?? DEFAULT_NESTING_PARALLELIZATION_LIMIT);

    let numbers = [];

    await flattenJsonMulti(request.body, numbers, 0, parallelizationThreshold, nestingParallelizationLimit, threadPool);

    const result = await parallelSortArray(numbers, threadPool.options.minThreads);

    reply.send({
        parallelizationThreshold,
        nestingParallelizationLimit,
        found: result.length,
        result: result,
    });
}