export default ({ thread, threads, iterations }) => {
    let threadIterations = Math.floor(iterations / threads);

    if (thread == threads - 1) {
        threadIterations += iterations % threads;
    }

    let finiteCount = 0;

    for (let i = 0; i < threadIterations; i++) {
        const randomRealNumber = Math.random() < 0.5 
            ? Math.random() * Number.MAX_VALUE * Number.MAX_VALUE
            : Math.random() * Number.MAX_VALUE;
        
        const result = Math.log(randomRealNumber);
    
        if (isFinite(result)) {
            finiteCount++;
        }
    }

    return finiteCount;
}