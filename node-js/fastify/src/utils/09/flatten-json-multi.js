export default async function flattenJsonMulti(json, numbers, depth, parallelizationThreshold, nestingParallelizationLimit, threadPool) {
    if (Array.isArray(json)) {
        if (depth <= nestingParallelizationLimit && json.length >= parallelizationThreshold) {
            const results = await Promise.all(json.map(element => threadPool.run({
                json: element,
                depth,
                parallelizationThreshold,
                nestingParallelizationLimit,
            })));
            numbers.push(...results.flat());
        } else {
            for (const element of json) {
                await flattenJsonMulti(element, numbers, depth, parallelizationThreshold, nestingParallelizationLimit, threadPool);
            }
        }

    } else if (json !== null && typeof json === 'object') {
        const values = Object.values(json);
        if (depth <= nestingParallelizationLimit && values.length >= parallelizationThreshold) {
            const results = await Promise.all(values.map(value => threadPool.run({
                json: value,
                depth: depth + 1,
                parallelizationThreshold,
                nestingParallelizationLimit,
            })));
            numbers.push(...results.flat());
        } else {
            for (const value of values) {
                await flattenJsonMulti(value, numbers, depth + 1, parallelizationThreshold, nestingParallelizationLimit, threadPool);
            }
        }

    } else if (!isNaN(json)) {
        numbers.push(Number(json));
    }
    
    return Promise.resolve();
}