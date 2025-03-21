const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 1;
const DEFAULT_UPPER_BOUND = 2;

module.exports = async (req, res) => {
    const iterations = Number(req.body.iterations) ?? DEFAULT_ITERATIONS;
    const lowerBound = Number(req.body.lowerBound) ?? DEFAULT_LOWER_BOUND;
    const upperBound = Number(req.body.upperBound) ?? DEFAULT_UPPER_BOUND;

    let quotient = Number.MAX_VALUE;

    for (let i = 0; i < iterations; i++) {
        const randomRealNumber = Math.random() * (upperBound - lowerBound) + lowerBound;
        quotient /= randomRealNumber;
    }

    res.json({ 
        iterations,
        lowerBound,
        upperBound,
        result: quotient,
    });
}