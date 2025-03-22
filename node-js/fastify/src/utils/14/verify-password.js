import { verify } from 'argon2';

export default async function verifyPassword({ hashedPassword, password }) {
    return await verify(hashedPassword, password);
}