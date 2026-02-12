#!/usr/bin/env node
/* eslint-disable no-console */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

function parseArgs(argv) {
  const args = {
    config: path.join('benchmarks', 'k6', 'config.json'),
    services: path.join('benchmarks', 'k6', 'services.json'),
    resultsDir: path.join('benchmarks', 'results'),
    passes: null,
  };

  for (let i = 2; i < argv.length; i += 1) {
    const token = argv[i];
    if (token === '--config') {
      args.config = argv[++i];
    } else if (token === '--services') {
      args.services = argv[++i];
    } else if (token === '--results-dir') {
      args.resultsDir = argv[++i];
    } else if (token === '--passes') {
      args.passes = argv[++i].split(',').map((v) => v.trim()).filter(Boolean);
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
  console.log(`Usage:\n  node benchmarks/k6/run-benchmarks.js [--config path] [--services path] [--results-dir path] [--passes memory,db]\n`);
}

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function nowStamp() {
  return new Date().toISOString().replace(/[:.]/g, '-');
}

function runCommand(command, args, options = {}) {
  const rendered = `${command} ${args.join(' ')}`;
  console.log(`\n$ ${rendered}`);
  const result = spawnSync(command, args, {
    stdio: 'inherit',
    env: options.env || process.env,
    shell: false,
  });

  if (result.status !== 0) {
    throw new Error(`Command failed (${result.status}): ${rendered}`);
  }
}

function runShell(command, env) {
  if (!command || !command.trim()) {
    return;
  }

  console.log(`\n$ ${command}`);
  const result = spawnSync('bash', ['-lc', command], {
    stdio: 'inherit',
    env: env || process.env,
  });

  if (result.status !== 0) {
    throw new Error(`Command failed (${result.status}): ${command}`);
  }
}

async function httpJson(method, url, body, authHeader) {
  const headers = { 'Content-Type': 'application/json' };
  if (authHeader) {
    headers.Authorization = authHeader;
  }

  const response = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  let parsed = null;
  const text = await response.text();
  if (text) {
    try {
      parsed = JSON.parse(text);
    } catch (_err) {
      parsed = null;
    }
  }

  return { response, body: parsed, text };
}

async function precheckCrud(baseUrl, basePath, authHeader) {
  const prefix = `${baseUrl.replace(/\/$/, '')}${basePath}`;

  const create = await httpJson('POST', `${prefix}/lamps`, { status: true }, authHeader);
  if (create.response.status !== 201 || !create.body || typeof create.body.id !== 'string') {
    throw new Error(`Precheck create failed (${create.response.status})`);
  }

  const lampId = create.body.id;

  const get = await httpJson('GET', `${prefix}/lamps/${lampId}`, null, authHeader);
  if (get.response.status !== 200) {
    throw new Error(`Precheck get failed (${get.response.status})`);
  }

  const update = await httpJson('PUT', `${prefix}/lamps/${lampId}`, { status: false }, authHeader);
  if (update.response.status !== 200) {
    throw new Error(`Precheck update failed (${update.response.status})`);
  }

  const list = await httpJson('GET', `${prefix}/lamps?pageSize=1`, null, authHeader);
  if (list.response.status !== 200 || !list.body || !Array.isArray(list.body.data)) {
    throw new Error(`Precheck list failed (${list.response.status})`);
  }

  const del = await httpJson('DELETE', `${prefix}/lamps/${lampId}`, null, authHeader);
  if (del.response.status !== 204) {
    throw new Error(`Precheck delete failed (${del.response.status})`);
  }
}

function parseMetric(summary, name) {
  const metric = summary.metrics[name];
  if (!metric || !metric.values) {
    return null;
  }
  return {
    avg: metric.values.avg ?? null,
    p95: metric.values['p(95)'] ?? null,
    p99: metric.values['p(99)'] ?? null,
    min: metric.values.min ?? null,
    max: metric.values.max ?? null,
  };
}

function parseRate(summary, name) {
  const metric = summary.metrics[name];
  if (!metric || !metric.values) {
    return null;
  }
  return metric.values.rate ?? null;
}

function median(numbers) {
  const vals = numbers.filter((n) => Number.isFinite(n)).slice().sort((a, b) => a - b);
  if (vals.length === 0) {
    return null;
  }
  const mid = Math.floor(vals.length / 2);
  return vals.length % 2 === 0 ? (vals[mid - 1] + vals[mid]) / 2 : vals[mid];
}

function maybeShuffle(list, enabled) {
  const copy = list.slice();
  if (!enabled) {
    return copy;
  }
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

function validateService(service) {
  if (!service.name) {
    throw new Error('Each service must have a name');
  }
}

function getPassSetupCommand(service, passName) {
  if (passName === 'memory') {
    return service.memorySetupCommand || '';
  }
  if (passName === 'db') {
    return service.dbSetupCommand || '';
  }
  return '';
}

function buildK6Env({ config, service, baseUrl, mode, targetRps, duration }) {
  const env = { ...process.env };
  env.RUN_MODE = mode;
  env.BASE_URL = baseUrl;
  env.BASE_PATH = config.basePath;
  env.TARGET_RPS = String(targetRps);
  env.DURATION = duration;
  env.PAGE_SIZE = String(config.workload.pageSize);
  env.SEED_FETCH_PAGES = String(config.workload.seedFetchPages);
  env.SEED_PAGE_SIZE = String(config.workload.seedPageSize);
  env.LIST_WEIGHT = String(config.workload.listPercent);
  env.GET_WEIGHT = String(config.workload.getPercent);
  env.CREATE_WEIGHT = String(config.workload.createPercent);
  env.UPDATE_WEIGHT = String(config.workload.updatePercent);
  env.DELETE_WEIGHT = String(config.workload.deletePercent);
  if (service.authHeader) {
    env.AUTH_HEADER = service.authHeader;
  }
  return env;
}

function runK6Phase({ scenarioPath, outputFile, env }) {
  runCommand('k6', ['run', scenarioPath, '--summary-export', outputFile], { env });
  return readJson(outputFile);
}

function aggregatePass(serviceRuns) {
  const fixedP95 = median(serviceRuns.map((r) => r.fixed.duration?.p95));
  const fixedP99 = median(serviceRuns.map((r) => r.fixed.duration?.p99));
  const fixedAvg = median(serviceRuns.map((r) => r.fixed.duration?.avg));
  const fixedErrorRate = median(serviceRuns.map((r) => r.fixed.errorRate));
  const maxStableRps = median(serviceRuns.map((r) => r.stress.maxStableRps));

  let extreme = null;
  const extremeRuns = serviceRuns.filter((r) => r.extreme);
  if (extremeRuns.length > 0) {
    extreme = {
      p95: median(extremeRuns.map((r) => r.extreme.duration?.p95)),
      p99: median(extremeRuns.map((r) => r.extreme.duration?.p99)),
      avg: median(extremeRuns.map((r) => r.extreme.duration?.avg)),
      errorRate: median(extremeRuns.map((r) => r.extreme.errorRate)),
    };
  }

  return {
    fixed: {
      p95: fixedP95,
      p99: fixedP99,
      avg: fixedAvg,
      errorRate: fixedErrorRate,
    },
    stress: {
      maxStableRps,
    },
    extreme,
  };
}

function formatNumber(value, digits = 2) {
  if (!Number.isFinite(value)) {
    return 'n/a';
  }
  return value.toFixed(digits);
}

function writeSummary(report, outputFile) {
  const lines = [];
  lines.push('# Benchmark Summary');
  lines.push('');
  lines.push(`Generated: ${report.generatedAt}`);
  lines.push('');

  for (const passName of Object.keys(report.aggregated)) {
    const rows = Object.entries(report.aggregated[passName]);
    rows.sort((a, b) => {
      const ap = a[1].fixed.p95;
      const bp = b[1].fixed.p95;
      if (!Number.isFinite(ap) && !Number.isFinite(bp)) return 0;
      if (!Number.isFinite(ap)) return 1;
      if (!Number.isFinite(bp)) return -1;
      return ap - bp;
    });

    lines.push(`## ${passName === 'memory' ? 'Memory Pass Ranking' : 'DB Pass Ranking'}`);
    lines.push('');
    lines.push('| Rank | Service | p95 (ms) | p99 (ms) | Avg (ms) | Error Rate | Max Stable RPS |');
    lines.push('|---|---|---:|---:|---:|---:|---:|');

    rows.forEach(([serviceName, metrics], idx) => {
      lines.push(
        `| ${idx + 1} | ${serviceName} | ${formatNumber(metrics.fixed.p95)} | ${formatNumber(metrics.fixed.p99)} | ${formatNumber(metrics.fixed.avg)} | ${formatNumber(metrics.fixed.errorRate * 100, 3)}% | ${formatNumber(metrics.stress.maxStableRps, 0)} |`
      );
    });
    lines.push('');
  }

  const memory = report.aggregated.memory || {};
  const db = report.aggregated.db || {};
  const services = Object.keys(memory).filter((name) => db[name]);

  if (services.length > 0) {
    lines.push('## Memory vs DB Delta');
    lines.push('');
    lines.push('| Service | Memory p95 (ms) | DB p95 (ms) | Delta (DB - Memory) |');
    lines.push('|---|---:|---:|---:|');

    for (const serviceName of services) {
      const mem = memory[serviceName].fixed.p95;
      const dbp = db[serviceName].fixed.p95;
      const delta = Number.isFinite(mem) && Number.isFinite(dbp) ? dbp - mem : null;
      lines.push(`| ${serviceName} | ${formatNumber(mem)} | ${formatNumber(dbp)} | ${formatNumber(delta)} |`);
    }
    lines.push('');
  }

  lines.push('## Extreme Concurrency Appendix (1000)');
  lines.push('');
  lines.push('| Pass | Service | p95 (ms) | p99 (ms) | Avg (ms) | Error Rate |');
  lines.push('|---|---|---:|---:|---:|---:|');

  for (const [passName, servicesMap] of Object.entries(report.aggregated)) {
    for (const [serviceName, metrics] of Object.entries(servicesMap)) {
      if (!metrics.extreme) {
        continue;
      }
      lines.push(
        `| ${passName} | ${serviceName} | ${formatNumber(metrics.extreme.p95)} | ${formatNumber(metrics.extreme.p99)} | ${formatNumber(metrics.extreme.avg)} | ${formatNumber(metrics.extreme.errorRate * 100, 3)}% |`
      );
    }
  }

  lines.push('');
  lines.push('Raw per-run k6 summaries are under `benchmarks/results/raw/`.');

  fs.writeFileSync(outputFile, `${lines.join('\n')}\n`, 'utf8');
}

async function main() {
  const args = parseArgs(process.argv);

  const config = readJson(args.config);
  const services = readJson(args.services);

  services.forEach(validateService);

  const configuredPasses = Array.isArray(config.passes) ? config.passes : ['memory', 'db'];
  const passes = args.passes && args.passes.length > 0 ? args.passes : configuredPasses;

  const stamp = nowStamp();
  const scenarioPath = path.join('benchmarks', 'k6', 'scenarios.js');
  const rawRoot = path.join(args.resultsDir, 'raw', stamp);

  ensureDir(rawRoot);

  runCommand('k6', ['version']);

  const report = {
    generatedAt: new Date().toISOString(),
    runId: stamp,
    config,
    passes,
    rawRoot,
    runs: {},
    aggregated: {},
  };

  for (const passName of passes) {
    if (!['memory', 'db'].includes(passName)) {
      throw new Error(`Unsupported pass: ${passName}`);
    }

    report.runs[passName] = {};
    const order = maybeShuffle(services, Boolean(config.randomizeServiceOrder));

    for (const service of order) {
      const baseUrl = passName === 'memory' ? service.memoryUrl : service.dbUrl;
      if (!baseUrl) {
        throw new Error(`Missing ${passName} URL for service ${service.name}`);
      }

      const passSetupCommand = getPassSetupCommand(service, passName);
      if (passSetupCommand) {
        runShell(passSetupCommand);
      }

      const serviceRuns = [];

      for (let iteration = 1; iteration <= Number(config.iterationsPerPass || 1); iteration += 1) {
        console.log(`\n=== ${passName.toUpperCase()} :: ${service.name} :: iteration ${iteration} ===`);

        if (passName === 'db' && service.dbSeedCommand) {
          runShell(service.dbSeedCommand);
        }

        await precheckCrud(baseUrl, config.basePath, service.authHeader || '');

        const iterDir = path.join(rawRoot, passName, service.name, `iter-${iteration}`);
        ensureDir(iterDir);

        const warmupFile = path.join(iterDir, 'warmup.json');
        const warmupSummary = runK6Phase({
          scenarioPath,
          outputFile: warmupFile,
          env: buildK6Env({
            config,
            service,
            baseUrl,
            mode: 'warmup',
            targetRps: config.warmup.rps,
            duration: config.warmup.duration,
          }),
        });

        const fixedFile = path.join(iterDir, 'fixed.json');
        const fixedSummary = runK6Phase({
          scenarioPath,
          outputFile: fixedFile,
          env: buildK6Env({
            config,
            service,
            baseUrl,
            mode: 'fixed',
            targetRps: config.fixed.rps,
            duration: config.fixed.duration,
          }),
        });

        let maxStableRps = null;
        const stressSteps = [];
        for (const rps of config.stress.rpsSteps) {
          const stressFile = path.join(iterDir, `stress-${rps}.json`);
          const stressSummary = runK6Phase({
            scenarioPath,
            outputFile: stressFile,
            env: buildK6Env({
              config,
              service,
              baseUrl,
              mode: 'stress',
              targetRps: rps,
              duration: config.stress.stepDuration,
            }),
          });

          const dur = parseMetric(stressSummary, 'stress_req_duration');
          const err = parseRate(stressSummary, 'stress_error_rate') || 0;
          const passed =
            Number.isFinite(dur?.p95) &&
            dur.p95 <= Number(config.slo.p95Ms) &&
            err <= Number(config.slo.errorRate);

          stressSteps.push({ rps, duration: dur, errorRate: err, passed });

          if (passed) {
            maxStableRps = rps;
          } else {
            break;
          }
        }

        let extreme = null;
        const shouldRunExtreme = Boolean(config.extreme?.enabled) &&
          (Boolean(config.extreme.runPerIteration) || iteration === 1);

        if (shouldRunExtreme) {
          const extremeFile = path.join(iterDir, 'extreme-1000.json');
          const extremeSummary = runK6Phase({
            scenarioPath,
            outputFile: extremeFile,
            env: buildK6Env({
              config,
              service,
              baseUrl,
              mode: 'extreme',
              targetRps: config.extreme.rps,
              duration: config.extreme.duration,
            }),
          });

          extreme = {
            duration: parseMetric(extremeSummary, 'extreme_req_duration'),
            errorRate: parseRate(extremeSummary, 'extreme_error_rate') || 0,
          };
        }

        const serviceRun = {
          iteration,
          warmup: {
            duration: parseMetric(warmupSummary, 'warmup_req_duration'),
            errorRate: parseRate(warmupSummary, 'warmup_error_rate') || 0,
          },
          fixed: {
            duration: parseMetric(fixedSummary, 'fixed_req_duration'),
            errorRate: parseRate(fixedSummary, 'fixed_error_rate') || 0,
          },
          stress: {
            maxStableRps,
            steps: stressSteps,
          },
          extreme,
        };

        serviceRuns.push(serviceRun);
      }

      report.runs[passName][service.name] = serviceRuns;
    }

    report.aggregated[passName] = {};
    for (const [serviceName, serviceRuns] of Object.entries(report.runs[passName])) {
      report.aggregated[passName][serviceName] = aggregatePass(serviceRuns);
    }
  }

  const reportPath = path.join(args.resultsDir, 'run-report.json');
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf8');

  const summaryPath = path.join(args.resultsDir, 'summary.md');
  writeSummary(report, summaryPath);

  console.log(`\nWrote report: ${reportPath}`);
  console.log(`Wrote summary: ${summaryPath}`);
}

main().catch((err) => {
  console.error(err.message || err);
  process.exit(1);
});
