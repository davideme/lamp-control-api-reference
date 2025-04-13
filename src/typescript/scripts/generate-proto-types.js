const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

// Ensure types directory exists
const typesDir = path.resolve(__dirname, '../types/grpc');
if (!fs.existsSync(typesDir)) {
  fs.mkdirSync(typesDir, { recursive: true });
}

// Path to proto file
const protoPath = path.resolve(__dirname, '../../../docs/api/lamp.proto');

// Run protobuf-ts to generate TypeScript types
try {
  console.log('Generating TypeScript types from Proto file...');
  execSync(
    `npx protobufjs --ts ${typesDir}/lamp.d.ts ${protoPath}`,
    { stdio: 'inherit' }
  );
  console.log('Types generated successfully at:', path.relative(process.cwd(), `${typesDir}/lamp.d.ts`));
} catch (error) {
  console.error('Failed to generate types:', error);
  process.exit(1);
}
