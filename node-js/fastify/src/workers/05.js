export default ({ warmup, thread, threads, iterations, upperBound, lowerBound }) => {
    if (warmup) {
        return;
    }

    let threadIterations = Math.floor(iterations / threads);

    if (thread == threads - 1) {
        threadIterations += iterations % threads;
    }

    let quotient = Number.MAX_VALUE;

    for (let i = 0; i < threadIterations; i++) {
        const randomRealNumber = Math.random() * (upperBound - lowerBound) + lowerBound;
        quotient /= randomRealNumber;
    }

    return {
        thread,
        quotient,
    };
}