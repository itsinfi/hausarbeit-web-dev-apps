const express = require('express');
const checkConnection = require('./api/01/check-connection');
const staticContent = require('./api/02/static-content');
const addition = require('./api/03/addition');
const multiplication = require('./api/04/multiplication');
const division = require('./api/05/division');
const powers = require('./api/06/powers');
const logarithms = require('./api/07/logarithms');
const primeNumbers = require('./api/08/prime-numbers');
const readJson = require('./api/09/read-json');
const writeJson = require('./api/10/write-json');
const sortWholeNumbers = require('./api/11/sort-whole-numbers');
const sortRealNumbers = require('./api/12/sort-real-numbers');
const linqStreams = require('./api/13/linq-streams');
const argon2id = require('./api/14/argon2id');
const staticContentMulti = require('./api/02/static-content-multi');
const additionMulti = require('./api/03/addition-multi');
const multiplicationMulti = require('./api/04/multiplication-multi');
const divisionMulti = require('./api/05/division-multi');
const powersMulti = require('./api/06/powers-multi');
const logarithmsMulti = require('./api/07/logarithms-multi');
const primeNumbersMulti = require('./api/08/prime-numbers-multi');
const readJsonMulti = require('./api/09/read-json-multi');
const writeJsonMulti = require('./api/10/write-json-multi');
const sortWholeNumbersMulti = require('./api/11/sort-whole-numbers-multi');
const sortRealNumbersMulti = require('./api/12/sort-real-numbers-multi');
const linqStreamsMulti = require('./api/13/linq-streams-multi');
const argon2idMulti = require('./api/14/argon2id-multi');

const app = express();
const router = express.Router();

app.use(express.json());

const routes = {
    '/01': checkConnection,
    '/01_multi': checkConnection,
    '/02': staticContent,
    '/02_multi': staticContentMulti,
    '/03': addition,
    '/03_multi': additionMulti,
    '/04': multiplication,
    '/04_multi': multiplicationMulti,
    '/05': division,
    '/05_multi': divisionMulti,
    '/06': powers,
    '/06_multi': powersMulti,
    '/07': logarithms,
    '/07_multi': logarithmsMulti,
    '/08': primeNumbers,
    '/08_multi': primeNumbersMulti,
    '/09': readJson,
    '/09_multi': readJsonMulti,
    '/10': writeJson,
    '/10_multi': writeJsonMulti,
    '/11': sortWholeNumbers,
    '/11_multi': sortWholeNumbersMulti,
    '/12': sortRealNumbers,
    '/12_multi': sortRealNumbersMulti,
    '/13': linqStreams,
    '/13_multi': linqStreamsMulti,
    '/14': argon2id,
    '/14_multi': argon2idMulti,
}

const routeHandler = (route) => (req, res, next) => {
    Promise.resolve(route(req, res)).catch(next);
};

const errorHandler = (err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({
        message: 'Internal Server Error',
        error: err.message,
    });
};

Object.entries(routes).forEach(([path, route]) => {
    router.post(path, routeHandler(route));
});

app.use('/api', router);

app.use(errorHandler);
process.on('uncaughtException', (err) => console.error(err));
process.on('unhandledRejection', (err) => console.error(err));

const port = 3000;
app.listen(port, async () => {
    console.log(`express running at http://localhost:${port}/`);
});