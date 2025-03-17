require('dotenv').config({ path: '../../.env' });
const fastify = require('fastify')();
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

const routes = {
    '/01':checkConnection,
    '/02':staticContent,
    '/03':addition,
    '/04':multiplication,
    '/05':division,
    '/06':powers,
    '/07':logarithms,
    '/08':primeNumbers,
    '/09':readJson,
    '/10':writeJson,
    '/11':sortWholeNumbers,
    '/12':sortRealNumbers,
    '/13':linqStreams,
    '/14':argon2id,
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