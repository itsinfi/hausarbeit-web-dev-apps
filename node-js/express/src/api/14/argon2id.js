const argon2 = 'argon2';
const crypto = 'node:crypto';

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

export default async (req, res) => {
    const password = String(req.body.password ?? '');
    const iterations = Number(req.body.iterations ?? DEFAULT_ARGON2_ITERATIONS);
    const parallelism = Number(req.body.parallelism ?? DEFAULT_ARGON2_PARALLELISM);
    const memoryInKb = Number(req.body.memoryInKb ?? DEFAULT_ARGON2_MEMORY_IN_KB);
    const saltSize = Number(req.body.saltSize ?? DEFAULT_SALT_SIZE);

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