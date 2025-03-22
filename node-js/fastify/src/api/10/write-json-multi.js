import createThreadPool from '../../utils/create-thread-pool.js';
import config from '../../config/config.js';
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

export default async (request, reply) => {
    const depth = Number(request.body.depth ?? DEFAULT_DEPTH);
    const objectsPerLevel = Number(request.body.objectsPerLevel ?? DEFAULT_OBJECTS_PER_LEVEL);
    const arraySize = Number(request.body.arraySize ?? DEFAULT_ARRAY_SIZE);
    const minValue = Number(request.body.minValue ?? DEFAULT_MIN_VALUE);
    const maxValue = Number(request.body.maxValue ?? DEFAULT_MAX_VALUE);

    const result = await generateJsonObject(depth, objectsPerLevel, arraySize, minValue, maxValue, threadPool);

    reply.send({
        depth,
        objectsPerLevel,
        arraySize,
        minValue,
        maxValue,
        result,
    });
}