const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 0;
const DEFAULT_UPPER_BOUND = 1;

export default (req, res) => {
    const iterations = Number(req.body.iterations ?? DEFAULT_ITERATIONS);
    const lowerBound = Number(req.body.lowerBound ?? DEFAULT_LOWER_BOUND);
    const upperBound = Number(req.body.upperBound ?? DEFAULT_UPPER_BOUND);

    let sum = 0;

    for (let i = 0; i < iterations; i++) {
        const randomRealNumber = Math.random() * (upperBound - lowerBound) + lowerBound;
        sum += randomRealNumber;
    }

    return { 
        iterations,
        lowerBound,
        upperBound,
        result: sum,
    };
}