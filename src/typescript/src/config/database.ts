// Database configuration settings
interface DatabaseConfig {
  mongodb: {
    uri: string;
    options: {
      useNewUrlParser: boolean;
      useUnifiedTopology: boolean;
    };
  };
  // Future implementations for MySQL and PostgreSQL can be added here
}

// Load configuration from environment variables with sensible defaults
const config: DatabaseConfig = {
  mongodb: {
    uri: process.env.MONGODB_URI || 'mongodb://lamp_user:lamp_password@localhost:27017/lamp_control',
    options: {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    }
  }
};

export default config;
