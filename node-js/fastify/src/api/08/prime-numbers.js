const DEFAULT_AMOUNT = 1000;

export default (req, res) => {
    const amount = Number(req.body.amount ?? DEFAULT_AMOUNT);

    let primes = [];
    let limit = amount;
    let iterations = 0;

    do {
        const squareRootOfLimit = Math.sqrt(limit);

        primes.length = 0;

        const sieve = Array.from(
            { length: limit + 1 },
            (_, i) => (i > 1)
        );

        for (let i = 2; i <= squareRootOfLimit; i++) {
            if (sieve[i]) {
                for (let j = i * i; j <= limit; j += i) {
                    sieve[j] = false;
                }
            }
        }

        for (let i = 2; i <= limit; i++) {
            if (sieve[i]) {
                primes.push(i);
            }
        }

        limit *= 2;
        iterations++;
    } while (primes.length < amount);

    const result = primes.slice(0, amount);

    return {
        iterations,
        found: result.length,
        result,
    };
}