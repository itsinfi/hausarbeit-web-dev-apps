const DEFAULT_ITERATIONS = 1000;

module.exports = async (req, res) => {
    const iterations = Number(req.body.iterations) ?? DEFAULT_ITERATIONS;

    let finiteCount = 0;

    for (let i = 0; i < iterations; i++) {
        const randomRealNumber = Math.random() < 0.5 
            ? Math.random() * 1e-100
            : Math.random() * 1e100;
        
        const result = Math.log(randomRealNumber);

        if (isFinite(result)) {
            finiteCount++;
        }
    }

    res.json({ 
        iterations,
        result: finiteCount,
    });
}