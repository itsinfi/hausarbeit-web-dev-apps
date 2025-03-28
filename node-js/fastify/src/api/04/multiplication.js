const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 1;
const DEFAULT_UPPER_BOUND = 2;

export default (request) => {
    const iterations = Number(request.body.iterations ?? DEFAULT_ITERATIONS);
    const lowerBound = Number(request.body.lowerBound ?? DEFAULT_LOWER_BOUND);
    const upperBound = Number(request.body.upperBound ?? DEFAULT_UPPER_BOUND);

    let product = 1;

    for (let i = 0; i < iterations; i++) {
        const randomRealNumber = Math.random() * (upperBound - lowerBound) + lowerBound;
        product *= randomRealNumber;
    }

    return { 
        iterations,
        lowerBound,
        upperBound,
        result: product,
    };
}