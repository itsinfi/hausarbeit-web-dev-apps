export default function generateJsonArray(size, minValue, maxValue) {
    return Array.from(
        { length: size },
        () => Math.random()  * (maxValue - minValue) + minValue,
    )
}