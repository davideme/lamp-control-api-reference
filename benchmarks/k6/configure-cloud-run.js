#!/usr/bin/env node
/* eslint-disable no-console */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

function parseArgs(argv) {
  const args = {
    services: path.join('benchmarks', 'k6', 'services.json'),
    config: path.join('benchmarks', 'k6', 'config.json'),
    project: '',
    execute: false,
  };

  for (let i = 2; i < argv.length; i += 1) {
    const token = argv[i];
    if (token === '--services') {
      args.services = argv[++i];
    } else if (token === '--config') {
      args.config = argv[++i];
    } else if (token === '--project') {
      args.project = argv[++i];
    } else if (token === '--execute') {
      args.execute = true;
    } else if (token === '--help' || token === '-h') {
      printHelp();
      process.exit(0);
    } else {
      throw new Error(`Unknown argument: ${token}`);
    }
  }

  return args;
}

function printHelp() {
  console.log(`Usage:\n  node benchmarks/k6/configure-cloud-run.js [--project my-project] [--execute]\n\nProject is resolved in this order: --project, cloudRun.projectId, cloudRun.projectNumber, GOOGLE_CLOUD_PROJECT.\nBy default this script prints commands only. Add --execute to run them.`);
}

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

function runCommand(command, args) {
  const rendered = `${command} ${args.join(' ')}`;
  console.log(`\n$ ${rendered}`);
  const result = spawnSync(command, args, { stdio: 'inherit' });
  if (result.status !== 0) {
    throw new Error(`Command failed (${result.status}): ${rendered}`);
  }
}

function validateCloudRunConfig(cloudRun) {
  const requiredKeys = [
    'maxInstances',
    'minInstances',
    'concurrency',
    'cpu',
    'memory',
    'timeout',
  ];

  for (const key of requiredKeys) {
    const value = cloudRun[key];
    if (value === undefined || value === null || String(value).trim() === '') {
      throw new Error(
        `Invalid cloudRun config: '${key}' is required in config.json (cloudRun.${key})`
      );
    }
  }
}

function resolveProject(argsProject, cloudRun) {
  if (argsProject && argsProject.trim()) {
    return argsProject.trim();
  }
  if (cloudRun.projectId && String(cloudRun.projectId).trim()) {
    return String(cloudRun.projectId).trim();
  }
  if (cloudRun.projectNumber && String(cloudRun.projectNumber).trim()) {
    return String(cloudRun.projectNumber).trim();
  }
  if (process.env.GOOGLE_CLOUD_PROJECT && process.env.GOOGLE_CLOUD_PROJECT.trim()) {
    return process.env.GOOGLE_CLOUD_PROJECT.trim();
  }
  throw new Error(
    "Missing project configuration. Set cloudRun.projectId (or projectNumber) in config.json, or pass --project, or set GOOGLE_CLOUD_PROJECT."
  );
}

function main() {
  const args = parseArgs(process.argv);
  const services = readJson(args.services);
  const config = readJson(args.config);
  const cloudRun = config.cloudRun || {};
  validateCloudRunConfig(cloudRun);
  const project = resolveProject(args.project, cloudRun);

  for (const service of services) {
    if (!service.cloudRunService) {
      console.log(`Skipping ${service.name}: cloudRunService is empty`);
      continue;
    }

    const region = service.cloudRunRegion || 'us-central1';
    const cmd = [
      'run',
      'services',
      'update',
      service.cloudRunService,
      '--project',
      project,
      '--region',
      region,
      '--max-instances',
      String(cloudRun.maxInstances),
      '--min-instances',
      String(cloudRun.minInstances),
      '--concurrency',
      String(cloudRun.concurrency),
      '--cpu',
      String(cloudRun.cpu),
      '--memory',
      String(cloudRun.memory),
      '--timeout',
      String(cloudRun.timeout),
      '--cpu-throttling',
    ];

    if (!args.execute) {
      console.log(`\n[dry-run] gcloud ${cmd.join(' ')}`);
      continue;
    }

    runCommand('gcloud', cmd);
  }

  if (!args.execute) {
    console.log('\nDry run complete. Re-run with --execute to apply updates.');
  }
}

main();
