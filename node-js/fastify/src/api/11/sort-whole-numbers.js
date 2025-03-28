const DEFAULT_ARRAY_SIZE = 1000;
const DEFAULT_MIN_VALUE = 0;
const DEFAULT_MAX_VALUE = 1000;

export default (request) => {
    const arraySize = Number(request.body.arraySize ?? DEFAULT_ARRAY_SIZE);
    const minValue = Number(request.body.minValue ?? DEFAULT_MIN_VALUE);
    const maxValue = Number(request.body.maxValue ?? DEFAULT_MAX_VALUE);

    const result = Array.from(
        { length: arraySize },
        () => Math.floor(Math.random()  * (maxValue - minValue) + minValue),
    ).sort((a, b) => a - b);

    return {
        arraySize,
        minValue,
        maxValue,
        result,
    };
}