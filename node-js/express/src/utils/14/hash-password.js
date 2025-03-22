import { hash } from 'argon2';
import { randomBytes } from 'node:crypto';

export default async function hashPassword({ password, argon2Options, saltSize }) {
    const salt = randomBytes(saltSize);
    return await hash(password, {
        ...argon2Options,
        salt,
    });
}