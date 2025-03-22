import generateJsonArray from "./generate-json-array.js";

export default async function generateJsonObject(depth, objectsPerLevel, arraySize, minValue, maxValue, threadPool) {
    let obj = {
        _: generateJsonArray(arraySize, minValue, maxValue),
    }

    if (depth > 1) {
        const objects = await Promise.all(Array(objectsPerLevel)
            .fill()
            .map(() => threadPool.run({
                depth: depth - 1,
                objectsPerLevel,
                arraySize,
                minValue,
                maxValue,
            }),
        ));

        for (let i = 0; i < objects.length; i++) {
            obj[i] = objects[i];
        }
    }

    return obj;
}