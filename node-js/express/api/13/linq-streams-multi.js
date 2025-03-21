function flattenJson(json, numbers) {
    if (Array.isArray(json)) {
        let sum = 0;
        json.forEach(element => {
            sum += Number(element);
        });
        const avg = sum / json.length;
        numbers.push(avg);
    } else if (json !== null && typeof json === 'object') {
        Object.keys(json).forEach(key => flattenJson(json[key], numbers));
    }
}

module.exports = async (req, res) => {
    let numbers = [];

    flattenJson(req.body, numbers);

    numbers.sort((a, b) => a - b);

    res.json({
        amount: numbers.length,
        result: numbers,
    });
}