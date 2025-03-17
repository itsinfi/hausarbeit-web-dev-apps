module.exports = async (req, res) => {
    const { name } = req.body;

    if (name) {
        res.json({ result: `Hello ${name}!` });
    }
}