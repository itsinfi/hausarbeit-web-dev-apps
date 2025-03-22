const DEFAULT_LENGTH = 1000;

const CHARACTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
export default async (request, reply) => {
    const length = Number(request.body.length ?? DEFAULT_LENGTH);

    let result = '';

    for (let i = 0; i < length; i++) {
        result += CHARACTERS[Math.floor(Math.random() * (CHARACTERS.length + 1))];
    }

    reply.send({
        length,
        result,
    });
}