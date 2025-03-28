export default (req) => {
    const { name } = req.body;

    if (name) {
        return { result: `Hello ${name}!` };
    }
}