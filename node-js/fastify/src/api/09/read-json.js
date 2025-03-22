function flattenJson(json, numbers) {
    if (Array.isArray(json)) {
        json.forEach(element => flattenJson(element, numbers));
    } else if (json !== null && typeof json === 'object') {
        Object.values(json).forEach(value => flattenJson(value, numbers));
    } else if (!isNaN(json)) {
        numbers.push(Number(json));
    }
}

export default async (request, reply) => {
    let numbers = [];

    flattenJson(request.body, numbers);

    reply.send({
        found: numbers.length,
        result: numbers,
    });
}