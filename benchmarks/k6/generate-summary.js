#!/usr/bin/env node
/* eslint-disable no-console */
const fs = require('fs');
const path = require('path');

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

  for (const passName of Object.keys(report.aggregated || {})) {
    const rows = Object.entries(report.aggregated[passName] || {});
    rows.sort((a, b) => {
      const ap = a[1].fixed?.p95;
      const bp = b[1].fixed?.p95;
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
        `| ${idx + 1} | ${serviceName} | ${formatNumber(metrics.fixed?.p95)} | ${formatNumber(metrics.fixed?.p99)} | ${formatNumber(metrics.fixed?.avg)} | ${formatNumber((metrics.fixed?.errorRate ?? NaN) * 100, 3)}% | ${formatNumber(metrics.stress?.maxStableRps, 0)} |`
      );
    });
    lines.push('');
  }

  const memory = report.aggregated?.memory || {};
  const db = report.aggregated?.db || {};
  const sharedServices = Object.keys(memory).filter((name) => db[name]);

  if (sharedServices.length > 0) {
    lines.push('## Memory vs DB Delta');
    lines.push('');
    lines.push('| Service | Memory p95 (ms) | DB p95 (ms) | Delta (DB - Memory) |');
    lines.push('|---|---:|---:|---:|');

    for (const serviceName of sharedServices) {
      const mem = memory[serviceName]?.fixed?.p95;
      const dbp = db[serviceName]?.fixed?.p95;
      const delta = Number.isFinite(mem) && Number.isFinite(dbp) ? dbp - mem : null;
      lines.push(`| ${serviceName} | ${formatNumber(mem)} | ${formatNumber(dbp)} | ${formatNumber(delta)} |`);
    }

    lines.push('');
  }

  lines.push('## Extreme Concurrency Appendix (1000)');
  lines.push('');
  lines.push('| Pass | Service | p95 (ms) | p99 (ms) | Avg (ms) | Error Rate |');
  lines.push('|---|---|---:|---:|---:|---:|');

  for (const [passName, servicesMap] of Object.entries(report.aggregated || {})) {
    for (const [serviceName, metrics] of Object.entries(servicesMap || {})) {
      if (!metrics.extreme) {
        continue;
      }
      lines.push(
        `| ${passName} | ${serviceName} | ${formatNumber(metrics.extreme?.p95)} | ${formatNumber(metrics.extreme?.p99)} | ${formatNumber(metrics.extreme?.avg)} | ${formatNumber((metrics.extreme?.errorRate ?? NaN) * 100, 3)}% |`
      );
    }
  }

  lines.push('');
  lines.push('Raw per-run k6 summaries are under `benchmarks/results/raw/`.');

  fs.writeFileSync(outputFile, `${lines.join('\n')}\n`, 'utf8');
}

function main() {
  if (process.argv.includes('--help') || process.argv.includes('-h')) {
    console.log(
      'Usage:\n  node benchmarks/k6/generate-summary.js [report.json] [summary.md]'
    );
    process.exit(0);
  }

  const reportPath = process.argv[2] || path.join('benchmarks', 'results', 'run-report.json');
  const outputPath = process.argv[3] || path.join('benchmarks', 'results', 'summary.md');

  if (!fs.existsSync(reportPath)) {
    throw new Error(`Report not found: ${reportPath}`);
  }

  const report = JSON.parse(fs.readFileSync(reportPath, 'utf8'));
  writeSummary(report, outputPath);
  console.log(`Wrote summary: ${outputPath}`);
}

main();
