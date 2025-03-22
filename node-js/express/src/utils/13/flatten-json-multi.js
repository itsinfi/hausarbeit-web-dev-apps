export default async function flattenJsonMulti(json, numbers, depth, parallelizationThreshold, nestingParallelizationLimit, threadPool) {
    if (Array.isArray(json)) {
        let sum = 0;
        json.forEach(element => {
            sum += Number(element);
        });
        const avg = sum / json.length;
        numbers.push(avg);

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
    }
    
    return Promise.resolve();
}