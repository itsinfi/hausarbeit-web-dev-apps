require('dotenv').config({ path: '../../.env' });
const fastify = require('fastify')();
const { MongoClient } = require('mongodb');

const dbHost = process.env.DB_HOST || 'localhost';

const uri = `mongodb://${dbHost}:27017/`;
const dbName = 'testdb';

const client = new MongoClient(uri);
let db;

async function connectDB() {
    await client.connect();
    db = client.db(dbName);
}

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
            return reply.status(400).send({
                error: 'Invalid or missing amount value',
            });
        }
    
        const collection = db.collection('example_collection');
        const docs = [];
    
        for (let i = 0; i < amount; i++) {
            docs.push({ param1, param2, param3 });
        }
    
        try {
            const result = await collection.insertMany(docs);
    
            const insertedIds = result.insertedIds;
    
            return reply.send({
                msg: `${result.insertedCount} new entries created successfully`,
                insertedIds: Object.values(insertedIds),
            });
        } catch (err) {
            console.error('Error inserting data:', err);
            return reply.status(500).send({
                error: 'Failed to insert data.',
            });
        }
    });

    done();

}, { prefix: '/app/api' });

const port = 3000;
fastify.listen({ port, host: '0.0.0.0' }, async () => {
    await connectDB();
    console.log(`fastify-mongo running at http://localhost:${port}/`);
});