#!/usr/bin/env node
/* eslint-disable no-console */
const fs = require('fs');
const path = require('path');
const { writeSummary } = require('./summary');

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
