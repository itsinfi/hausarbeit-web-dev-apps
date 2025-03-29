import cluster from 'node:cluster';
import process from 'node:process';
import startServer from './start-server.js';
import config from './config/config.js';

console.log('CLUSTER_COUNT:', process.env.CLUSTER_COUNT);
console.log('EVENT_LOOP_THREAD_POOL_SIZE:', process.env.EVENT_LOOP_THREAD_POOL_SIZE);
console.log('OPERATIONAL_THREAD_POOL_SIZE:', process.env.OPERATIONAL_THREAD_POOL_SIZE);

process.env.UV_THREADPOOL_SIZE = process.env.EVENT_LOOP_THREAD_POOL_SIZE;

const clusterCount = process.env.CLUSTER_COUNT ?? 1;

if (cluster.isPrimary) {
    console.log(`Primary ${process.pid} is running`);

    for (let i = 0; i < clusterCount; i++) {
        cluster.fork();
    }

    cluster.on('exit', (worker) => {
        console.log(`worker ${worker.process.pid} died`);
    });
} else {
    startServer();

    console.log(`Worker ${process.pid} started`);
}