const DEFAULT_LENGTH = 1000;

const CHARACTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
module.exports = async (req, res) => {
    const length = Number(req.body.length) ?? DEFAULT_LENGTH;

    result = '';

    for (let i = 0; i < length; i++) {
        result += CHARACTERS[Math.floor(Math.random() * (CHARACTERS.length + 1))];
    }

    res.json({
        length,
        result,
    });
}