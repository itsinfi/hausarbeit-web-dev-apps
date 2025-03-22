const primeNumbers = './prime-numbers.js';
import createThreadPool from '../../utils/create-thread-pool.js';

const DEFAULT_PARALLELIZATION_THRESHOLD = 3;
const DEFAULT_NESTING_PARALLELIZATION_LIMIT = 3;

const piscina = createThreadPool('./src/workers/08.js');

(async () => {
    await Promise.all(Array(piscina.options.minThreads)
        .fill()
        .map(() => piscina.run({ warmup: true }))
    );
})();

async function flattenJson(json, numbers, depth, parallelizationThreshold, nestingParallelizationLimit) {
    if (Array.isArray(json)) {
        json.forEach(element => flattenJson(element, numbers, depth, parallelizationThreshold, nestingParallelizationLimit));//TODO:
    } else if (json !== null && typeof json === 'object') {
        if (depth <= nestingParallelizationLimit && Object.keys(json).length >= parallelizationThreshold) {
            Promise.all(Array.from(
                Object.keys(json),
                (v, _) => piscina.run({
                    v,
                    numbers,
                    depth: depth + 1,
                    parallelizationThreshold,
                    nestingParallelizationLimit
                })
            ));
        } else {
            Object.keys(json).forEach(key => flattenJson(json[key], numbers, depth + 1, parallelizationThreshold, nestingParallelizationLimit));
        }
    } else {
        numbers.push(Number(json));
    }
}

export default async (req, res) => {
    const parallelizationThreshold = Number(req.body.parallelizationThreshold) ?? DEFAULT_PARALLELIZATION_THRESHOLD;
    const nestingParallelizationLimit = Number(req.body.nestingParallelizationLimit) ?? DEFAULT_NESTING_PARALLELIZATION_LIMIT;

    let numbers = [];

    flattenJson(req.body, numbers, 0, parallelizationThreshold, nestingParallelizationLimit);

    res.json({
        parallelizationThreshold,
        nestingParallelizationLimit,
        found: numbers.length,
        result: numbers,
    });
}