require('dotenv').config({ path: '../../.env' });
const fastify = require('fastify')();
const mysql = require('mysql2');

const dbHost = process.env.DB_HOST || 'localhost';

const pool = mysql.createPool({
    host: dbHost,
    user: 'root',
    password: 'root',
    database: 'testdb',
    port: 3306,
});

const asyncPool = pool.promise();

const param1 = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\nDuis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis a";
const param2 = 42;
const param3 = '2025-01-01';

fastify.register((instance, options, done) => {

    instance.get('/rest', async (request, reply) => {
        return reply.send({ msg: 'Hello World!' });
    });

    instance.post('/rest', async (request, reply) => {
        const { amount } = request.body;
    
        if (!amount || isNaN(amount) || amount <= 0) {
            return reply.code(400).send({
                error: 'Invalid or missing amount value',
            });
        }
    
        const queries = [];
        const values = [];
    
        for (let i = 0; i < amount; i++) {
            queries.push('(?, ?, ?)');
            values.push(param1, param2, param3);
        }
    
        const query = `
            INSERT INTO example_table (param1, param2, param3)
            VALUES ${queries.join(', ')}`;
        
        try {
            const [result] = await asyncPool.execute(query, values);
    
            const insertedIds = [];
            const firstInsertedId = result.insertId;
            for (let i = 0; i < result.affectedRows; i++) {
                insertedIds.push(firstInsertedId + i);
            }
    
            return reply.send({
                msg: `${result.affectedRows} new entries created successfully`,
                insertedIds,
            });
        } catch (err) {
            console.error('Error inserting data:', err);
                return reply.code(500).send({
                    error: 'Failed to insert data.',
                });
        }
    });

    done();

}, { prefix: '/app/api' });

const port = 3000;
fastify.listen({ port, host: '0.0.0.0' }, async () => {
    console.log(`fastify-mariadb running at http://localhost:${port}/`);
});