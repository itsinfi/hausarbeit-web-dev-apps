import parallelSortArray from "../../utils/parallel-sort-array.js";

const DEFAULT_ARRAY_SIZE = 1000;
const DEFAULT_MIN_VALUE = 0;
const DEFAULT_MAX_VALUE = 1000;

export default async (request) => {
    const threads = Number(request.body.threads ?? 1);
    const arraySize = Number(request.body.arraySize ?? DEFAULT_ARRAY_SIZE);
    const minValue = Number(request.body.minValue ?? DEFAULT_MIN_VALUE);
    const maxValue = Number(request.body.maxValue ?? DEFAULT_MAX_VALUE);

    const arr = Array.from(
        { length: arraySize },
        () => Math.floor(Math.random() * (maxValue - minValue) + minValue),
    );

    const result = await parallelSortArray(arr, threads);

    return {
        threads,
        arraySize,
        minValue,
        maxValue,
        result,
    };
}