const DEFAULT_DEPTH = 3;
const DEFAULT_OBJECTS_PER_LEVEL = 4;
const DEFAULT_ARRAY_SIZE = 4;
const DEFAULT_MIN_VALUE = 0;
const DEFAULT_MAX_VALUE = 100;

function generateJsonObject(depth, objectsPerLevel, arraySize, minValue, maxValue) {
    let obj = {
        _: generateJsonArray(arraySize, minValue, maxValue),
    }

    if (depth > 1) {
        for (let i = 1; i <= objectsPerLevel; i++) {
            obj[i] = generateJsonObject(depth - 1, objectsPerLevel, arraySize, minValue, maxValue);
        }
    }

    return obj;
}

function generateJsonArray(size, minValue, maxValue) {
    return Array.from(
        { length: size },
        () => Math.random()  * (maxValue - minValue) + minValue,
    )
}

module.exports = async (request, reply) => {
    const depth = Number(request.body.depth) ?? DEFAULT_DEPTH;
    const objectsPerLevel = Number(request.body.objectsPerLevel) ?? DEFAULT_OBJECTS_PER_LEVEL;
    const arraySize = Number(request.body.arraySize) ?? DEFAULT_ARRAY_SIZE;
    const minValue = Number(request.body.minValue) ?? DEFAULT_MIN_VALUE;
    const maxValue = Number(request.body.maxValue) ?? DEFAULT_MAX_VALUE;

    const result = generateJsonObject(depth, objectsPerLevel, arraySize, minValue, maxValue);

    reply.send({
        depth,
        objectsPerLevel,
        arraySize,
        minValue,
        maxValue,
        result,
    });
}