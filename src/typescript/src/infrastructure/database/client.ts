import { PrismaClient } from '@prisma/client';

let prisma: PrismaClient | undefined;

function normalizeDatabaseUrl(databaseUrl: string): string {
  const hasHostQueryParam = /(?:\?|&)host=/.test(databaseUrl);
  if (!hasHostQueryParam) {
    return databaseUrl;
  }

  const withCredentialsPattern = /^(postgres(?:ql)?:\/\/[^/?#]*@)\/(.+)$/i;
  if (withCredentialsPattern.test(databaseUrl)) {
    return databaseUrl.replace(withCredentialsPattern, '$1localhost/$2');
  }

  const withoutCredentialsPattern = /^(postgres(?:ql)?:\/\/)\/(.+)$/i;
  if (withoutCredentialsPattern.test(databaseUrl)) {
    return databaseUrl.replace(withoutCredentialsPattern, '$1localhost/$2');
  }

  return databaseUrl;
}

export function getPrismaClient(): PrismaClient {
  if (!prisma) {
    if (!process.env.DATABASE_URL) {
      throw new Error(
        'DATABASE_URL environment variable is not set. Cannot initialize Prisma Client.',
      );
    }
    const databaseUrl = normalizeDatabaseUrl(process.env.DATABASE_URL);
    prisma = new PrismaClient({
      log: process.env.NODE_ENV === 'development' ? ['query', 'info', 'warn', 'error'] : ['error'],
      datasources: {
        db: {
          url: databaseUrl,
        },
      },
    });
  }
  return prisma;
}

export async function closePrismaClient(): Promise<void> {
  if (prisma) {
    await prisma.$disconnect();
    prisma = undefined;
  }
}

// Lazy initialization using Proxy to avoid eager initialization issues
// This ensures the client is only created when actually used, preventing
// errors when DATABASE_URL is not set (e.g., in unit tests using in-memory storage)
export const prismaClient = new Proxy({} as PrismaClient, {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  get(_target, prop): any {
    const client = getPrismaClient();
    return client[prop as keyof PrismaClient];
  },
});
