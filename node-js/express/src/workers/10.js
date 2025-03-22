import generateJsonObject from "../utils/10/generate-json-object-multi.js";

export default async function run({ warmup, depth, objectsPerLevel, arraySize, minValue, maxValue }) {
    if (warmup) {
        return;
    }

    return await generateJsonObject(depth, objectsPerLevel, arraySize, minValue, maxValue, {
        run: (params) => run(params), 
    });
}