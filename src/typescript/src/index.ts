import { app } from './infrastructure/server';
import { appLogger } from './utils/logger';

const port = process.env.PORT || 3000;

app.listen(port, () => {
  appLogger.info(`Server is running on port ${port}`, {
    port,
    env: process.env.NODE_ENV || 'development',
  });
});
