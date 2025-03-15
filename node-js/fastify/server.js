require('dotenv').config({ path: '../../.env' });
const fastify = require('fastify')();

fastify.register((instance, options, done) => {
    
    instance.get('/rest', async (request, reply) => {
        return reply.send({ msg: 'Hello World!' });
    });
    
    instance.post('/rest', async (request, reply) => {
        return reply.send({ msg: 'Hello World!' });
    });

    done();

}, { prefix: '/api' });

const port = 3000;
fastify.listen({ port, host: '0.0.0.0' }, async () => {
    console.log(`fastify-mongo running at http://localhost:${port}/`);
});