import express, { Router, json } from 'express';
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
    const app = express();
    const router = Router();

    app.use(json());

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

    const syncRouteHandler = (syncRoute) => (req, res, next) => {
        const startTime = process.hrtime();

        try {
            const result = syncRoute(req, res);
            setProcessingTimeHeader(startTime, res);
            res.json(result);
        } catch (err) {
            setProcessingTimeHeader(startTime, reply);
            next(err);
        }
    };

    const asyncRouteHandler = (asyncRoute) => (req, res, next) => {
        const startTime = process.hrtime();

        Promise.resolve(asyncRoute(req, res))
            .then((result) => {
                setProcessingTimeHeader(startTime, res)
                res.json(result);
            })
            .catch((err) => {
                setProcessingTimeHeader(startTime, reply);
                next(err);
            });
    };

    const errorHandler = (err, req, res, next) => {
        console.error(err.stack);
        res.status(500).json({
            message: 'Internal Server Error',
            error: err.message,
        });
    };

    function setProcessingTimeHeader(startTime, res) {
        const [seconds, nanoseconds] = process.hrtime(startTime);
        const elapsedTimeInNanoseconds = seconds + nanoseconds / 1e9;
        res.setHeader('processing-time', elapsedTimeInNanoseconds);
    }

    Object.entries(syncRoutes).forEach(([path, route]) => {
        router.post(path, syncRouteHandler(route));
    });
    
    Object.entries(asyncRoutes).forEach(([path, route]) => {
        router.post(path, asyncRouteHandler(route));
    });

    app.use('/api', router);

    app.use(errorHandler);
    process.on('uncaughtException', (err) => console.error(err));
    process.on('unhandledRejection', (err) => console.error(err));

    const port = 3000;
    app.listen(port, async () => {
        console.log(`express running at http://localhost:${port}/`);
    });
}