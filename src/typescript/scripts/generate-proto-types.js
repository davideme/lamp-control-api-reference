const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

// Ensure generated directory exists
const generatedDir = path.resolve(__dirname, '../types/grpc');
if (!fs.existsSync(generatedDir)) {
  fs.mkdirSync(generatedDir, { recursive: true });
}

// Path to proto file and proto directory
const protoDir = path.resolve(__dirname, '../../../docs/api');
const protoFile = path.resolve(protoDir, 'lamp.proto');

// Run ts-proto to generate TypeScript types
try {
  console.log('Generating TypeScript types from Proto file using ts-proto...');
  execSync(
    `npx protoc \\
    --plugin=protoc-gen-ts_proto=./node_modules/.bin/protoc-gen-ts_proto \\
    --ts_proto_out=src/infrastructure/grpc/generated \\
    --ts_proto_opt=env=node,outputServices=grpc-js,esModuleInterop=true \\
    --proto_path=${protoDir} \\
    ${protoFile}`,
    { stdio: 'inherit', shell: true }
  );
  console.log('Types generated successfully in:', path.relative(process.cwd(), generatedDir));
} catch (error) {
  console.error('Failed to generate types:', error);
  process.exit(1);
}
