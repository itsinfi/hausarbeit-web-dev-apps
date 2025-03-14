const express = require('express');
const mysql = require('mysql2');

const app = express();
const router = express.Router();

app.use(express.json());

const pool = mysql.createPool({
    host: 'host.docker.internal',
    user: 'root',
    password: 'root',
    database: 'testdb',
    port: 3306,
});

const asyncPool = pool.promise();

const param1 = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\nDuis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis a";
const param2 = 42;
const param3 = '2025-01-01';

router.get('/rest', async (req, res) => {
    res.json({ msg: 'Hello World!' });
});

router.post('/rest', async (req, res) => {
    const { amount } = req.body;

    if (!amount || isNaN(amount) || amount <= 0) {
        return res.status(400).json({
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

        return res.json({
            msg: `${result.affectedRows} new entries created successfully`,
            insertedIds,
        });
    } catch (err) {
        console.error('Error inserting data:', err);
        return res.status(500).json({
            error: 'Failed to insert data.',
        });
    }
});

app.use('/app/api', router);

const port = 3000;
app.listen(port, async () => {
    console.log(`express-mysql running at http://localhost:${port}/`);
});