export default (req, res) => {
    const { name } = req.body;

    if (name) {
        return { result: `Hello ${name}!` };
    }
}