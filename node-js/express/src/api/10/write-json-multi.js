import createThreadPool from '../../utils/create-thread-pool.js';
import generateJsonObject from '../../utils/10/generate-json-object-multi.js';

const DEFAULT_DEPTH = 3;
const DEFAULT_OBJECTS_PER_LEVEL = 4;
const DEFAULT_ARRAY_SIZE = 4;
const DEFAULT_MIN_VALUE = 0;
const DEFAULT_MAX_VALUE = 100;

const threadPool = createThreadPool('./src/workers/10.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (req) => {
    const depth = Number(req.body.depth ?? DEFAULT_DEPTH);
    const objectsPerLevel = Number(req.body.objectsPerLevel ?? DEFAULT_OBJECTS_PER_LEVEL);
    const arraySize = Number(req.body.arraySize ?? DEFAULT_ARRAY_SIZE);
    const minValue = Number(req.body.minValue ?? DEFAULT_MIN_VALUE);
    const maxValue = Number(req.body.maxValue ?? DEFAULT_MAX_VALUE);

    const result = await generateJsonObject(depth, objectsPerLevel, arraySize, minValue, maxValue, threadPool);

    return {
        depth,
        objectsPerLevel,
        arraySize,
        minValue,
        maxValue,
        result,
    };
}