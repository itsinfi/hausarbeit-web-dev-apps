const DEFAULT_ITERATIONS = 1000;
const DEFAULT_LOWER_BOUND = 1;
const DEFAULT_UPPER_BOUND = 2;

export default async (request, reply) => {
    const iterations = Number(request.body.iterations ?? DEFAULT_ITERATIONS);
    const lowerBound = Number(request.body.lowerBound ?? DEFAULT_LOWER_BOUND);
    const upperBound = Number(request.body.upperBound ?? DEFAULT_UPPER_BOUND);

    let quotient = Number.MAX_VALUE;

    for (let i = 0; i < iterations; i++) {
        const randomRealNumber = Math.random() * (upperBound - lowerBound) + lowerBound;
        quotient /= randomRealNumber;
    }

    reply.send({ 
        iterations,
        lowerBound,
        upperBound,
        result: quotient,
    });
}