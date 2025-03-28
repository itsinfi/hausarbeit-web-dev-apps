function flattenJson(json, numbers) {
    if (Array.isArray(json)) {
        json.forEach(element => flattenJson(element, numbers));
    } else if (json !== null && typeof json === 'object') {
        Object.values(json).forEach(value => flattenJson(value, numbers));
    } else if (!isNaN(json)) {
        numbers.push(Number(json));
    }
}

export default (request) => {
    let numbers = [];

    flattenJson(request.body, numbers);

    return {
        found: numbers.length,
        result: numbers,
    };
}