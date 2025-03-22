import flattenJsonMulti from '../utils/13/flatten-json-multi.js';

export default async function run({ warmup, json, depth, parallelizationThreshold, nestingParallelizationLimit }) {
    if (warmup) {
        return;
    }

    let numbers = [];

    await flattenJsonMulti(json, numbers, depth, parallelizationThreshold, nestingParallelizationLimit, {
        run: (params) => run(params), 
    });

    return numbers;
}