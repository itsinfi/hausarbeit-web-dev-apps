export default async (request, reply) => {
    const { name } = request.body;

    if (name) {
        reply.send({ result: `Hello ${name}!` });
    }
}