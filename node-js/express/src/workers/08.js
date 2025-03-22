export default ({ warmup, threadIndex, threads, limit, squareRootOfLimit, sieve }) => {
    if (warmup) {
        return;
    }
    
    let start = Math.floor(squareRootOfLimit + 1 + threadIndex * ((limit - squareRootOfLimit) / threads));
    let end = Math.floor((threadIndex == threads - 1) ? limit : start + ((limit - squareRootOfLimit) / threads));

    for (let i = 2; i <= squareRootOfLimit; i++) {
        if (sieve[i]) {
            let firstMultiple = Math.max(i + i, (start + i - 1) / i * i);
            
            for (let j = firstMultiple; j <= end; j += i) {
                sieve[j] = false;
            }
        }
    }
}