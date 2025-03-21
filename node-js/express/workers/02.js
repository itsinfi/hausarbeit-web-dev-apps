const { parentPort } = require('worker_threads');

const CHARACTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

parentPort.on('message', ({ thread, threads, length }) => {
    let threadLength = Math.floor(length / threads);

    if (thread == threads - 1) {
        threadLength += length % threads;
    }

    result = '';

    for (let i = 0; i < threadLength; i++) {
        result += CHARACTERS[Math.floor(Math.random() * (CHARACTERS.length + 1))];
    }

    parentPort.postMessage(result);
});