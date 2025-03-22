import hashPassword from "../utils/14/hash-password.js";
import verifyPassword from "../utils/14/verify-password.js";

export default async ({ warmup, method, data }) => {
    if (warmup) {
        return;
    }

    switch (method) {
        case 'hash':
            return await hashPassword(data);
        
        case 'verify':
            return await verifyPassword(data);
    
        default:
            break;
    }
}