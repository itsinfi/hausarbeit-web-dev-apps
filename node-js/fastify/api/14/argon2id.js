const argon2 = require('argon2');
const crypto = require('node:crypto');

const DEFAULT_ARGON2_ITERATIONS = 3;
const DEFAULT_ARGON2_PARALLELISM = 4;
const DEFAULT_ARGON2_MEMORY_IN_KB = 65536;
const DEFAULT_SALT_SIZE = 128;

async function hashPassword(password, argon2Options, saltSize) {
    const salt = crypto.randomBytes(saltSize);
    return await argon2.hash(password, {
        ...argon2Options,
        salt,
    });
}

async function verifyPassword(hash, password) {
    return await argon2.verify(hash, password);
}

module.exports = async (request, reply) => {
    const password = request.body.password;
    const iterations = request.body.iterations ?? DEFAULT_ARGON2_ITERATIONS;
    const parallelism = request.body.parallelism ?? DEFAULT_ARGON2_PARALLELISM;
    const memoryInKb = request.body.memoryInKb ?? DEFAULT_ARGON2_MEMORY_IN_KB;
    const saltSize = request.body.saltSize ?? DEFAULT_SALT_SIZE;

    if (!password) {
        return;
    }

    const argon2Options = {
        type: argon2.argon2id,
        iterations,
        parallelism,
        memoryCost: memoryInKb,
        hashLength: saltSize / 4,
    };

    const hashedPassword = await hashPassword(password, argon2Options, saltSize);

    const checkAuth = await verifyPassword(hashedPassword, password, argon2Options, saltSize);

    reply.send({
        password,
        iterations,
        parallelism,
        memoryInKb,
        saltSize,
        result: {
            hashedPassword,
            checkAuth,
        }
    });
}