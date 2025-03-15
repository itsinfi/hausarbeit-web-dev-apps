require('dotenv').config({ path: '../../.env' });
const express = require('express');

const app = express();
const router = express.Router();

app.use(express.json());

router.get('/rest', async (req, res) => {
    res.json({ msg: 'Hello World!' });
});

router.post('/rest', async (req, res) => {
    res.json({ msg: 'Hello World!' });
});

app.use('/api', router);

const port = 3000;
app.listen(port, async () => {
    console.log(`express-mongo running at http://localhost:${port}/`);
});