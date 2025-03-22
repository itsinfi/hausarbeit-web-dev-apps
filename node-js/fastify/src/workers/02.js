const CHARACTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

export default ({ warmup, thread, threads, length }) => {
    if (warmup) {
        return;
    }

    let threadLength = Math.floor(length / threads);
    
    if (thread == threads - 1) {
        threadLength += length % threads;
    }

    let result = '';

    for (let i = 0; i < threadLength; i++) {
        result += CHARACTERS[Math.floor(Math.random() * (CHARACTERS.length + 1))];
    }

    return result;
}