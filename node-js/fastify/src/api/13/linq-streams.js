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

export default async (request, reply) => {
    let numbers = [];

    flattenJson(request.body, numbers);

    numbers.sort((a, b) => a - b);

    reply.send({
        amount: numbers.length,
        result: numbers,
    });
}