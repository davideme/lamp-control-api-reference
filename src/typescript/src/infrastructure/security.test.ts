/**
 * @jest-environment node
 */
import { describe, it, expect } from '@jest/globals';
import Security from './security.ts';

describe('Security', () => {
  it('should accept any API key', async () => {
    const security = new Security();
    const result = await security.apiKeyAuth({} as any, {} as any, 'any-key');
    expect(result).toBe(true);
  });
});
