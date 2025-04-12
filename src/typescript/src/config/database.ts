// Database configuration settings
interface DatabaseConfig {
  mongodb: {
    uri: string;
    options: {
      useNewUrlParser: boolean;
      useUnifiedTopology: boolean;
    };
  };
  postgresql: {
    connectionString: string;
    // Add other PostgreSQL-specific options here if needed
  };
  // Future implementation for MySQL can be added here
}

// Load configuration from environment variables with sensible defaults
const config: DatabaseConfig = {
  mongodb: {
    uri: process.env.MONGODB_URI || 'mongodb://lamp_user:lamp_password@localhost:27017/lamp_control',
    options: {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    }
  },
  postgresql: {
    connectionString: process.env.POSTGRESQL_URI || 
      'postgresql://lamp_user:lamp_password@localhost:5432/lamp_control'
  }
};

export default config;
