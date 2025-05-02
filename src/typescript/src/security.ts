export default class Security {
    async apiKeyAuth(request: any, reply: any, key: string) {
        // For development, we'll accept any API key
        return true;
    }
} 