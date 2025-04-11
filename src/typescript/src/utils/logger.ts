import winston from 'winston';

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json(),
  ),
  defaultMeta: { service: 'lamp-control-api' },
  transports: [
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.printf(({ timestamp, level, message, ...meta }) => {
          const metaStr = Object.keys(meta).length ? JSON.stringify(meta) : '';
          return `${timestamp} ${level}: ${message} ${metaStr}`;
        }),
      ),
    }),
  ],
});

// Add request context to logs
export interface LogContext {
  requestId?: string;
  userId?: string;
  [key: string]: unknown;
}

class Logger {
  private context: LogContext = {};

  setContext(context: LogContext): void {
    this.context = { ...this.context, ...context };
  }

  clearContext(): void {
    this.context = {};
  }

  private formatError(error: Error): Record<string, unknown> {
    return {
      name: error.name,
      message: error.message,
      stack: error.stack,
    };
  }

  private formatMessage(
    message: string,
    meta?: Record<string, unknown> | Error,
  ): Record<string, unknown> {
    const formattedMeta = meta instanceof Error ? this.formatError(meta) : meta;
    return {
      ...this.context,
      ...(formattedMeta || {}),
      message,
    };
  }

  debug(message: string, meta?: Record<string, unknown> | Error): void {
    logger.debug(this.formatMessage(message, meta));
  }

  info(message: string, meta?: Record<string, unknown> | Error): void {
    logger.info(this.formatMessage(message, meta));
  }

  warn(message: string, meta?: Record<string, unknown> | Error): void {
    logger.warn(this.formatMessage(message, meta));
  }

  error(message: string, meta?: Record<string, unknown> | Error): void {
    logger.error(this.formatMessage(message, meta));
  }
}

export const appLogger = new Logger();
