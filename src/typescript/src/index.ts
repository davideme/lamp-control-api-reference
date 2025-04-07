import { app } from './infrastructure/server';
import { appLogger } from './utils/logger';

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  appLogger.info(`Server is running on port ${PORT}`);
}); 