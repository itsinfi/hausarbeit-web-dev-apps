const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 1;
const DEFAULT_UPPER_BOUND = 2;

export default (req) => {
    const iterations = Number(req.body.iterations ?? DEFAULT_ITERATIONS);
    const lowerBound = Number(req.body.lowerBound ?? DEFAULT_LOWER_BOUND);
    const upperBound = Number(req.body.upperBound ?? DEFAULT_UPPER_BOUND);

    let sum = 0;

    for (let i = 0; i < iterations; i++) {
        const randomRealNumber = Math.random() * (upperBound - lowerBound) + lowerBound;
        sum += Math.exp(randomRealNumber);
    }

    return { 
        iterations,
        lowerBound,
        upperBound,
        result: sum,
    };
}