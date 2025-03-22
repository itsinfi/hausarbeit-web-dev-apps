import createThreadPool from '../utils/create-thread-pool.js';

const threadPool = createThreadPool('./src/workers/parallel-sort-array.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async function parallelSortArray(arr, length) {
    const chunkSize = Math.ceil(arr.length / length);
    const chunks = Array.from(
        { length },
        (_, i) => arr.slice(i * chunkSize, (i + 1) * chunkSize)
    ).filter(chunk => chunk.length > 0);
    
    return (await Promise.all(chunks.map(chunk => threadPool.run(chunk))))
        .flat()
        .sort((a, b) => a - b);
}