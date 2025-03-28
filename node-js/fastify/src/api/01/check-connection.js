export default (request) => {
    const { name } = request.body;

    if (name) {
        return { result: `Hello ${name}!` };
    }
}