import argon2 from 'argon2';
import crypto from 'node:crypto';

const DEFAULT_ARGON2_ITERATIONS = 3;
const DEFAULT_ARGON2_PARALLELISM = 4;
const DEFAULT_ARGON2_MEMORY_IN_KB = 65536;
const DEFAULT_SALT_SIZE = 128;
const DEFAULT_TASK_AMOUNT = 10;

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

export default async (request) => {
    const password = String(request.body.password ?? '');
    const iterations = Number(request.body.iterations ?? DEFAULT_ARGON2_ITERATIONS);
    const parallelism = Number(request.body.parallelism ?? DEFAULT_ARGON2_PARALLELISM);
    const memoryInKb = Number(request.body.memoryInKb ?? DEFAULT_ARGON2_MEMORY_IN_KB);
    const saltSize = Number(request.body.saltSize ?? DEFAULT_SALT_SIZE);
    const taskAmount = Number(request.body.taskAmount ?? DEFAULT_TASK_AMOUNT);

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

    let result = [];

    for (let i = 0; i < taskAmount; i++) {
        const hashedPassword = await hashPassword(password, argon2Options, saltSize);
        const checkAuth = await verifyPassword(hashedPassword, password, argon2Options, saltSize);
        
        result.push({ hashedPassword, checkAuth });
    }

    return {
        password,
        iterations,
        parallelism,
        memoryInKb,
        saltSize,
        taskAmount,
        result,
    };
}