#!/usr/bin/env node
/**
 * Command-line interface for Lamp Control API
 *
 * Supports three operation modes:
 * - serve: Run migrations and start server (default)
 * - migrate: Run migrations only
 * - serve-only: Start server without migrations
 */

import { execSync } from 'child_process';
import { buildApp } from './infrastructure/app.ts';

const PORT = parseInt(process.env.PORT || '8080', 10);
const HOST = process.env.HOST || '0.0.0.0';

/**
 * Run Prisma migrations only and exit
 */
async function runMigrationsOnly(): Promise<void> {
  console.warn('Running migrations only...');

  if (process.env.USE_POSTGRES !== 'true') {
    console.warn('No PostgreSQL configuration found (USE_POSTGRES not set), nothing to migrate');
    return;
  }

  try {
    console.warn('Running Prisma migrations...');
    execSync('npx prisma migrate deploy', {
      stdio: 'inherit',
      env: process.env,
    });
    console.warn('Migrations completed successfully');
  } catch (error) {
    console.error('Migration failed:', error);
    process.exit(1);
  }
}

/**
 * Start the server with optional migrations
 */
async function startServer(runMigrations: boolean): Promise<void> {
  if (runMigrations) {
    console.warn('Starting server with automatic migrations...');
    if (process.env.USE_POSTGRES === 'true') {
      try {
        console.warn('Running Prisma migrations...');
        execSync('npx prisma migrate deploy', {
          stdio: 'inherit',
          env: process.env,
        });
        console.warn('Migrations completed');
      } catch (error) {
        console.error('Migration failed:', error);
        process.exit(1);
      }
    }
  } else {
    console.warn('Starting server without running migrations...');
  }

  const server = await buildApp();
  server.listen({ port: PORT, host: HOST }, (err: Error | null, address: string) => {
    if (err) {
      server.log.error(err);
      process.exit(1);
    }
    server.log.info(`Server listening at ${address}`);
  });
}

/**
 * Main CLI entry point
 */
async function main(): Promise<void> {
  const args = process.argv.slice(2);
  const modeArg = args.find((arg) => arg.startsWith('--mode='));
  const mode = modeArg ? modeArg.split('=')[1] : 'serve';

  switch (mode) {
    case 'migrate':
      await runMigrationsOnly();
      break;
    case 'serve':
      await startServer(true);
      break;
    case 'serve-only':
      await startServer(false);
      break;
    default:
      console.error(`Invalid mode: ${mode}. Valid modes are: serve, migrate, serve-only`);
      process.exit(1);
  }
}

main().catch((error) => {
  console.error('Fatal error:', error);
  process.exit(1);
});
