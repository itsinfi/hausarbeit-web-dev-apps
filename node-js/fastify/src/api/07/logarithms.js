const DEFAULT_ITERATIONS = 1000;

export default async (request, reply) => {
    const iterations = Number(request.body.iterations ?? DEFAULT_ITERATIONS);

    let finiteCount = 0;

    for (let i = 0; i < iterations; i++) {
        const randomRealNumber = Math.random() < 0.5 
            ? Math.random() * Number.MAX_VALUE * Number.MAX_VALUE
            : Math.random() * Number.MAX_VALUE;
        
        const result = Math.log(randomRealNumber);

        if (isFinite(result)) {
            finiteCount++;
        }
    }

    reply.send({ 
        iterations,
        result: finiteCount,
    });
}