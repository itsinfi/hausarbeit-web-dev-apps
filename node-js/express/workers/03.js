export default ({ thread, threads, iterations, upperBound, lowerBound }) => {
    let threadIterations = Math.floor(iterations / threads);

    if (thread == threads - 1) {
        threadIterations += iterations % threads;
    }

    let sum = 0;

    for (let i = 0; i < threadIterations; i++) {
        const randomRealNumber = Math.random() * (upperBound - lowerBound) + lowerBound;
        sum += randomRealNumber;
    }

    return sum;
}