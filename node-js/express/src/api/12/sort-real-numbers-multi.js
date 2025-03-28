import parallelSortArray from "../../utils/parallel-sort-array.js";

const DEFAULT_ARRAY_SIZE = 1000;
const DEFAULT_MIN_VALUE = 0;
const DEFAULT_MAX_VALUE = 1000;

export default async (req, res) => {
    const threads = Number(req.body.threads ?? process.env.THREAD_POOL_SIZE ?? 1);
    const arraySize = Number(req.body.arraySize ?? DEFAULT_ARRAY_SIZE);
    const minValue = Number(req.body.minValue ?? DEFAULT_MIN_VALUE);
    const maxValue = Number(req.body.maxValue ?? DEFAULT_MAX_VALUE);

    const arr = Array.from(
        { length: arraySize },
        () => Math.random() * (maxValue - minValue) + minValue,
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