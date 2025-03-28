import createThreadPool from '../../utils/create-thread-pool.js';
import { argon2id } from 'argon2';

const DEFAULT_ARGON2_ITERATIONS = 3;
const DEFAULT_ARGON2_PARALLELISM = 4;
const DEFAULT_ARGON2_MEMORY_IN_KB = 65536;
const DEFAULT_SALT_SIZE = 128;
const DEFAULT_TASK_AMOUNT = 10;

const threadPool = createThreadPool('./src/workers/14.js');

(async () => {
    await Promise.all(Array(threadPool.options.minThreads)
        .fill()
        .map(() => threadPool.run({ warmup: true }))
    );
})();

export default async (req, res) => {
    const password = String(req.body.password ?? '');
    const iterations = Number(req.body.iterations ?? DEFAULT_ARGON2_ITERATIONS);
    const parallelism = Number(req.body.parallelism ?? DEFAULT_ARGON2_PARALLELISM);
    const memoryInKb = Number(req.body.memoryInKb ?? DEFAULT_ARGON2_MEMORY_IN_KB);
    const saltSize = Number(req.body.saltSize ?? DEFAULT_SALT_SIZE);
    const taskAmount = Number(req.body.taskAmount ?? DEFAULT_TASK_AMOUNT);

    if (!password) {
        return;
    }

    const argon2Options = {
        type: argon2id,
        iterations,
        parallelism,
        memoryCost: memoryInKb,
        hashLength: saltSize / 4,
    };

    const task = async () => {
        const hashedPassword = await threadPool.run({ method: 'hash', data: { password, argon2Options, saltSize } });
        const checkAuth = await threadPool.run({ method: 'verify', data: { hashedPassword, password } });
        return { hashedPassword, checkAuth };
    };

    const result = await Promise.all(Array(taskAmount).fill().map(task));

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