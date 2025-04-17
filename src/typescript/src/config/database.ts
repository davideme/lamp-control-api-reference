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
  mysql: {
    connectionString: string;
    // Add other MySQL-specific options here if needed
  };
}

// Load configuration from environment variables with sensible defaults
const config: DatabaseConfig = {
  mongodb: {
    uri:
      process.env.MONGODB_URI || 'mongodb://lamp_user:lamp_password@localhost:27017/lamp_control',
    options: {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    },
  },
  postgresql: {
    connectionString:
      process.env.POSTGRESQL_URI ||
      'postgresql://lamp_user:lamp_password@localhost:5432/lamp_control',
  },
  mysql: {
    connectionString:
      process.env.MYSQL_URI || 'mysql://lamp_user:lamp_password@localhost:3306/lamp_control',
  },
};

export default config;
