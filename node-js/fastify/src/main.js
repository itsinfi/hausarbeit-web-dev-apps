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

const fastify = Fastify();

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

Object.entries(routes).forEach(([path, route]) => {
    fastify.post(`/api${path}`, route);
});

const port = 3000;
fastify.listen({ port, host: '0.0.0.0' }, async (err) => {
    if (err) {
        console.error(err);
    }
    console.log(`fastify running at http://localhost:${port}/`);
});