import { PrismaClient } from '@prisma/client';

let prisma: PrismaClient | undefined;

/**
 * Normalizes PostgreSQL connection URLs with empty host for Prisma compatibility.
 *
 * Cloud SQL Unix socket URLs can use:
 *   postgresql://user:pass@/db?host=/cloudsql/INSTANCE_CONNECTION_NAME
 * Prisma rejects URLs whose authority has an empty host.
 *
 * This rewrites empty-host URLs to use `localhost` in the authority while
 * preserving the Unix socket path in the `host` query parameter.
 *
 * The credentials regex intentionally excludes raw '/', '?' and '#' characters.
 * Credentials should be URL-encoded (for example `%2F`), which is supported.
 *
 * @param databaseUrl The database URL to normalize.
 * @returns The normalized URL, or the original URL when no rewrite is needed.
 */
function normalizeDatabaseUrl(databaseUrl: string): string {
  const hasHostQueryParam = /(?:\?|&)host=/.test(databaseUrl);
  if (!hasHostQueryParam) {
    return databaseUrl;
  }

  const withCredentialsPattern = /^(postgres(?:ql)?:\/\/[^/?#]*@)\/(.+)$/i;
  const normalizedWithCredentials = databaseUrl.replace(withCredentialsPattern, '$1localhost/$2');
  if (normalizedWithCredentials !== databaseUrl) {
    return normalizedWithCredentials;
  }

  const withoutCredentialsPattern = /^(postgres(?:ql)?:\/\/)\/(.+)$/i;
  const normalizedWithoutCredentials = databaseUrl.replace(
    withoutCredentialsPattern,
    '$1localhost/$2',
  );
  if (normalizedWithoutCredentials !== databaseUrl) {
    return normalizedWithoutCredentials;
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
