function flattenJson(json, numbers) {
    if (Array.isArray(json)) {
        json.forEach(element => flattenJson(element, numbers));
    } else if (json !== null && typeof json === 'object') {
        Object.keys(json).forEach(key => flattenJson(json[key], numbers));
    } else {
        numbers.push(Number(json));
    }
}

module.exports = async (request, reply) => {
    let numbers = [];

    flattenJson(request.body, numbers);

    reply.send({
        result: numbers,
    });
}