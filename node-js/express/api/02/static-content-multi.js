const { Worker } = require('worker_threads');
const staticContent = require("./static-content");

const DEFAULT_THREADS = 1;
const DEFAULT_LENGTH = 1000;

module.exports = async (req, res) => {
    const threads = Number(req.body.threads) ?? DEFAULT_THREADS;
    const length = Number(req.body.length) ?? DEFAULT_LENGTH;

    if (threads <= 1) {
        return staticContent(req, res);
    }

    let result = '';
    
    let promises = [];
    
    for (let i = 0; i < threads; i++) {
        promises.push(new Promise((resolve, reject) => {
            const worker = new Worker('./workers/02.js');
            worker.postMessage({ thread: i, threads, length });
            worker.on('message', resolve);
            worker.on('error', reject);
        }));
    }

    const results = await Promise.all(promises);

    results.forEach(r => {
        result += r;
    });

    res.json({
        length,
        result,
    });
}