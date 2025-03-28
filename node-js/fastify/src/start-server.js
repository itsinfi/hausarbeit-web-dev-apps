import Fastify from 'fastify';
import checkConnection from './api/01/check-connection.js';
import staticContent from './api/02/static-content.js';
import addition from './api/03/addition.js';
import multiplication from './api/04/multiplication.js';
import division from './api/05/division.js';
import powers from './api/06/powers.js';
import logarithms from './api/07/logarithms.js';
import primeNumbers from './api/08/prime-numbers.js';
import readJson from './api/09/read-json.js';
import writeJson from './api/10/write-json.js';
import sortWholeNumbers from './api/11/sort-whole-numbers.js';
import sortRealNumbers from './api/12/sort-real-numbers.js';
import linqStreams from './api/13/linq-streams.js';
import argon2id from './api/14/argon2id.js';
import staticContentMulti from './api/02/static-content-multi.js';
import additionMulti from './api/03/addition-multi.js';
import multiplicationMulti from './api/04/multiplication-multi.js';
import divisionMulti from './api/05/division-multi.js';
import powersMulti from './api/06/powers-multi.js';
import logarithmsMulti from './api/07/logarithms-multi.js';
import primeNumbersMulti from './api/08/prime-numbers-multi.js';
import readJsonMulti from './api/09/read-json-multi.js';
import writeJsonMulti from './api/10/write-json-multi.js';
import sortWholeNumbersMulti from './api/11/sort-whole-numbers-multi.js';
import sortRealNumbersMulti from './api/12/sort-real-numbers-multi.js';
import linqStreamsMulti from './api/13/linq-streams-multi.js';
import argon2idMulti from './api/14/argon2id-multi.js';

export default function startServer() {
    const fastify = Fastify();

    const syncRoutes = {
        '/01': checkConnection,
        '/02': staticContent,
        '/03': addition,
        '/04': multiplication,
        '/05': division,
        '/06': powers,
        '/07': logarithms,
        '/08': primeNumbers,
        '/09': readJson,
        '/10': writeJson,
        '/11': sortWholeNumbers,
        '/12': sortRealNumbers,
        '/13': linqStreams,
        '/14': argon2id,
    };

    const asyncRoutes = {
        '/01_multi': checkConnection,
        '/02_multi': staticContentMulti,
        '/03_multi': additionMulti,
        '/04_multi': multiplicationMulti,
        '/05_multi': divisionMulti,
        '/06_multi': powersMulti,
        '/07_multi': logarithmsMulti,
        '/08_multi': primeNumbersMulti,
        '/09_multi': readJsonMulti,
        '/10_multi': writeJsonMulti,
        '/11_multi': sortWholeNumbersMulti,
        '/12_multi': sortRealNumbersMulti,
        '/13_multi': linqStreamsMulti,
        '/14_multi': argon2idMulti,
    };

    const syncRouteHandler = (syncRoute) => (request, reply) => {
        const startTime = process.hrtime();

        try {
            const result = syncRoute(request, reply);
            setProcessingTimeHeader(startTime, reply);
            reply.send(result);
        } catch (err) {
            setProcessingTimeHeader(startTime, reply);
            errorHandler(err, reply);
        }
    };

    const asyncRouteHandler = (asyncRoute) => (request, reply) => {
        const startTime = process.hrtime();

        Promise.resolve(asyncRoute(request, reply))
            .then((result) => {
                setProcessingTimeHeader(startTime, reply)
                reply.send(result);
            })
            .catch((err) => {
                setProcessingTimeHeader(startTime, reply);
                errorHandler(err, reply);
            });
    };

    const errorHandler = (err, reply) => {
        console.error(err.stack);
        reply.status(500).send({
            message: 'Internal Server Error',
            error: err.message,
        });
    };

    function setProcessingTimeHeader(startTime, reply) {
        const [seconds, nanoseconds] = process.hrtime(startTime);
        const elapsedTimeInNanoseconds = seconds + nanoseconds / 1e9;
        reply.header('processing-time', elapsedTimeInNanoseconds);
    }

    Object.entries(syncRoutes).forEach(([path, route]) => {
        fastify.post(`/api${path}`, syncRouteHandler(route));
    });
    
    Object.entries(asyncRoutes).forEach(([path, route]) => {
        fastify.post(`/api${path}`, asyncRouteHandler(route));
    });

    const port = 3000;
    fastify.listen({ port, host: '0.0.0.0' }, async (err) => {
        if (err) {
            console.error(err);
        }
        console.log(`fastify running at http://localhost:${port}/`);
    });
}