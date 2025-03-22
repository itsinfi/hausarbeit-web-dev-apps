export default (arr) => {
    if (arr?.warmup) {
        return;
    }

    arr.sort((a, b) => a - b);
    return arr;
}