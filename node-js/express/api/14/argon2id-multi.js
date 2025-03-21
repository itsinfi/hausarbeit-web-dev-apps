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

module.exports = async (req, res) => {
    const password = req.body.password;
    const iterations = req.body.iterations ?? DEFAULT_ARGON2_ITERATIONS;
    const parallelism = req.body.parallelism ?? DEFAULT_ARGON2_PARALLELISM;
    const memoryInKb = req.body.memoryInKb ?? DEFAULT_ARGON2_MEMORY_IN_KB;
    const saltSize = req.body.saltSize ?? DEFAULT_SALT_SIZE;

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

    res.json({
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