#!/usr/bin/env node
/**
 * report.js — Reads k6 window summary files and generates a JIT warmup comparison report.
 *
 * Reads: benchmarks/jit-profile/results/window_{0..N}.json
 * Writes: benchmarks/jit-profile/results/report.md
 *         benchmarks/jit-profile/results/report.csv
 *
 * Usage:
 *   node report.js
 */

'use strict';

const fs = require('fs');
const path = require('path');

const RESULTS_DIR = path.join(__dirname, 'results');
const CONFIG = JSON.parse(fs.readFileSync(path.join(__dirname, 'config.json'), 'utf8'));

const LANGUAGES = ['typescript', 'python', 'go', 'java', 'kotlin', 'csharp'];
const LANGUAGE_LABELS = {
  typescript: 'TypeScript',
  python: 'Python',
  go: 'Go',
  java: 'Java',
  kotlin: 'Kotlin',
  csharp: 'C#',
};

// ─── Load window data ─────────────────────────────────────────────────────────

function loadWindows() {
  const windows = [];
  let i = 0;
  while (true) {
    const filePath = path.join(RESULTS_DIR, `window_${i}.json`);
    if (!fs.existsSync(filePath)) break;
    const raw = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    windows.push(raw);
    i++;
  }
  if (windows.length === 0) {
    console.error('No window_*.json files found in', RESULTS_DIR);
    process.exit(1);
  }
  return windows;
}

// ─── Load run metadata (optional) ────────────────────────────────────────────

function loadMeta() {
  const metaPath = path.join(RESULTS_DIR, 'run-meta.json');
  if (!fs.existsSync(metaPath)) return null;
  return JSON.parse(fs.readFileSync(metaPath, 'utf8'));
}

// ─── Compute JIT tier annotations per window ──────────────────────────────────

function computeJitAnnotations(windows, rps, windowSeconds) {
  // Returns: Map<windowIndex, [{ label, languages }]>
  const annotations = new Map();
  for (const tier of CONFIG.jit_thresholds) {
    const triggerSeconds = tier.method_calls / rps;
    // Which window does this transition happen in?
    const windowIdx = Math.floor(triggerSeconds / windowSeconds);
    if (windowIdx < windows.length) {
      if (!annotations.has(windowIdx)) annotations.set(windowIdx, []);
      annotations.get(windowIdx).push(tier);
    }
  }
  return annotations;
}

// ─── Format p95 value ─────────────────────────────────────────────────────────

function fmt(value) {
  if (value === null || value === undefined) return '  —  ';
  return `${Math.round(value)}ms`.padStart(5);
}

// ─── Build annotation string for a language in a given window ─────────────────

function buildAnnotation(windowIdx, lang, jitAnnotations) {
  const tiers = jitAnnotations.get(windowIdx) || [];
  const matching = tiers.filter((t) => t.languages.includes(lang));
  if (matching.length === 0) return '';
  return ' ← ' + matching.map((t) => t.label).join(', ');
}

// ─── Generate Markdown report ─────────────────────────────────────────────────

function generateMarkdown(windows, meta) {
  const rps = meta?.target_rps || CONFIG.target_rps_per_language;
  const windowSeconds = meta?.window_seconds || CONFIG.window_seconds;
  const jitAnnotations = computeJitAnnotations(windows, rps, windowSeconds);

  const lines = [];

  lines.push('# JIT Warmup Benchmark — Language Comparison');
  lines.push('');
  if (meta) {
    lines.push(`**Run date**: ${meta.timestamp}`);
    lines.push(`**Load**: ${rps} RPS constant per language, in-memory backend`);
    lines.push(`**Environment**: Cloud Run 1 vCPU / 512MB, europe-west1`);
    lines.push(`**Windows**: ${windows.length} × ${windowSeconds}s = ${windows.length * windowSeconds}s total`);
    lines.push(`**Requests/window**: ~${rps * windowSeconds} per language`);
  }
  lines.push('');

  // ── p95 Table ─────────────────────────────────────────────────────────────
  lines.push('## p95 Latency Over Time (ms)');
  lines.push('');
  lines.push(`> ${rps} RPS constant · ~${rps * windowSeconds} requests/window · in-memory backend`);
  lines.push(`> JIT annotations mark the window where each compiler tier is expected to activate`);
  lines.push('');

  // Header row
  const timeHeaders = windows.map((w) => `t=${w.elapsed_seconds}s`);
  const colWidth = 8;
  const langCol = 12;

  const headerCells = ['Language'.padEnd(langCol), ...timeHeaders.map((h) => h.padStart(colWidth))];
  lines.push('| ' + headerCells.join(' | ') + ' |');
  lines.push('|' + '-'.repeat(langCol + 2) + '|' + windows.map(() => '-'.repeat(colWidth + 2)).join('|') + '|');

  // Data rows
  for (const lang of LANGUAGES) {
    const label = LANGUAGE_LABELS[lang] || lang;
    const cells = [label.padEnd(langCol)];
    for (const w of windows) {
      const m = w.metrics && w.metrics[lang];
      const p95 = m ? m.p95 : null;
      const annotation = buildAnnotation(w.window, lang, jitAnnotations);
      const valStr = p95 !== null ? `${Math.round(p95)}ms` : '—';
      // Annotation goes after the value in the same cell
      cells.push((valStr + annotation).padStart(colWidth));
    }
    lines.push('| ' + cells.join(' | ') + ' |');
  }

  lines.push('');

  // ── JIT legend ────────────────────────────────────────────────────────────
  lines.push('### Expected JIT Equilibrium Points');
  lines.push('');
  lines.push(`At ${rps} RPS, assuming each HTTP request invokes each hot-path method once:`);
  lines.push('');
  lines.push(`| Tier | Method invocations | Time at ${rps} RPS | Window | Languages |`);
  lines.push('|------|--------------------|----------------|--------|-----------|');
  for (const tier of CONFIG.jit_thresholds) {
    const t = tier.method_calls / rps;
    const w = Math.floor(t / windowSeconds);
    const tStr = t < 60 ? `~${Math.round(t)}s` : `~${(t / 60).toFixed(1)}min`;
    lines.push(`| ${tier.label} | ~${tier.method_calls.toLocaleString()} | ${tStr} | window t=${w * windowSeconds}s | ${tier.languages.join(', ')} |`);
  }
  lines.push('');
  lines.push('**Go** and **Python** have no JIT — expect flat latency across all windows.');
  lines.push('');

  // ── p50 Table ─────────────────────────────────────────────────────────────
  lines.push('## p50 (Median) Latency Over Time (ms)');
  lines.push('');
  const headerCells2 = ['Language'.padEnd(langCol), ...timeHeaders.map((h) => h.padStart(colWidth))];
  lines.push('| ' + headerCells2.join(' | ') + ' |');
  lines.push('|' + '-'.repeat(langCol + 2) + '|' + windows.map(() => '-'.repeat(colWidth + 2)).join('|') + '|');

  for (const lang of LANGUAGES) {
    const label = LANGUAGE_LABELS[lang] || lang;
    const cells = [label.padEnd(langCol)];
    for (const w of windows) {
      const m = w.metrics && w.metrics[lang];
      const p50 = m ? m.p50 : null;
      const valStr = p50 !== null ? `${Math.round(p50)}ms` : '—';
      cells.push(valStr.padStart(colWidth));
    }
    lines.push('| ' + cells.join(' | ') + ' |');
  }
  lines.push('');

  // ── p99 Table ─────────────────────────────────────────────────────────────
  lines.push('## p99 Latency Over Time (ms)');
  lines.push('');
  const headerCells3 = ['Language'.padEnd(langCol), ...timeHeaders.map((h) => h.padStart(colWidth))];
  lines.push('| ' + headerCells3.join(' | ') + ' |');
  lines.push('|' + '-'.repeat(langCol + 2) + '|' + windows.map(() => '-'.repeat(colWidth + 2)).join('|') + '|');

  for (const lang of LANGUAGES) {
    const label = LANGUAGE_LABELS[lang] || lang;
    const cells = [label.padEnd(langCol)];
    for (const w of windows) {
      const m = w.metrics && w.metrics[lang];
      const p99 = m ? m.p99 : null;
      const valStr = p99 !== null ? `${Math.round(p99)}ms` : '—';
      cells.push(valStr.padStart(colWidth));
    }
    lines.push('| ' + cells.join(' | ') + ' |');
  }
  lines.push('');

  // ── Warmup summary ────────────────────────────────────────────────────────
  lines.push('## Warmup Summary');
  lines.push('');
  lines.push('| Language | First window p95 | Final window p95 | Improvement | JIT effect |');
  lines.push('|----------|-----------------|------------------|-------------|------------|');

  for (const lang of LANGUAGES) {
    const label = LANGUAGE_LABELS[lang] || lang;
    const firstWindow = windows[0];
    const lastWindow = windows[windows.length - 1];
    const first = firstWindow.metrics?.[lang]?.p95;
    const last = lastWindow.metrics?.[lang]?.p95;

    let improvement = '—';
    let jitEffect = '—';
    if (first !== null && first !== undefined && last !== null && last !== undefined) {
      const ratio = first / last;
      improvement = ratio >= 1.05 ? `${ratio.toFixed(1)}× faster` : 'flat';
      if (ratio >= 2) jitEffect = 'Strong JIT warmup detected';
      else if (ratio >= 1.2) jitEffect = 'Moderate JIT improvement';
      else if (ratio >= 1.05) jitEffect = 'Minor improvement';
      else jitEffect = 'Flat (no JIT or already warm)';
    }

    const firstStr = first !== undefined && first !== null ? `${Math.round(first)}ms` : '—';
    const lastStr = last !== undefined && last !== null ? `${Math.round(last)}ms` : '—';
    lines.push(`| ${label} | ${firstStr} | ${lastStr} | ${improvement} | ${jitEffect} |`);
  }
  lines.push('');

  return lines.join('\n');
}

// ─── Generate CSV ─────────────────────────────────────────────────────────────

function generateCsv(windows) {
  const rows = [];
  rows.push(['window', 'elapsed_seconds', 'language', 'p50_ms', 'p95_ms', 'p99_ms', 'avg_ms'].join(','));

  for (const w of windows) {
    for (const lang of LANGUAGES) {
      const m = w.metrics && w.metrics[lang];
      if (!m) {
        rows.push([w.window, w.elapsed_seconds, lang, '', '', '', ''].join(','));
        continue;
      }
      rows.push([
        w.window,
        w.elapsed_seconds,
        lang,
        m.p50 !== null ? Math.round(m.p50 * 100) / 100 : '',
        m.p95 !== null ? Math.round(m.p95 * 100) / 100 : '',
        m.p99 !== null ? Math.round(m.p99 * 100) / 100 : '',
        m.avg !== null ? Math.round(m.avg * 100) / 100 : '',
      ].join(','));
    }
  }

  return rows.join('\n') + '\n';
}

// ─── Main ─────────────────────────────────────────────────────────────────────

const windows = loadWindows();
const meta = loadMeta();

console.log(`Loaded ${windows.length} windows.`);

const rps = meta?.target_rps || CONFIG.target_rps_per_language;
const windowSeconds = meta?.window_seconds || CONFIG.window_seconds;

// Print quick summary to stdout
console.log('');
console.log('p95 Latency Over Time (ms):');
const header = ['Language'.padEnd(12), ...windows.map((w) => `t=${w.elapsed_seconds}s`.padStart(8))].join(' ');
console.log(header);
console.log('-'.repeat(header.length));

for (const lang of LANGUAGES) {
  const label = (LANGUAGE_LABELS[lang] || lang).padEnd(12);
  const values = windows.map((w) => {
    const m = w.metrics?.[lang];
    return m?.p95 !== null && m?.p95 !== undefined ? `${Math.round(m.p95)}ms`.padStart(8) : '     —  ';
  });
  console.log([label, ...values].join(' '));
}

console.log('');

// Print JIT tier timings
const jitAnnotations = computeJitAnnotations(windows, rps, windowSeconds);
if (jitAnnotations.size > 0) {
  console.log('JIT tier annotations:');
  for (const [windowIdx, tiers] of jitAnnotations) {
    for (const tier of tiers) {
      const elapsed = windowIdx * windowSeconds;
      console.log(`  t=${elapsed}s → ${tier.label} (${tier.languages.join(', ')})`);
    }
  }
  console.log('');
}

// Write report files
const mdPath = path.join(RESULTS_DIR, 'report.md');
const csvPath = path.join(RESULTS_DIR, 'report.csv');

fs.writeFileSync(mdPath, generateMarkdown(windows, meta));
fs.writeFileSync(csvPath, generateCsv(windows));

console.log(`report.md → ${mdPath}`);
console.log(`report.csv → ${csvPath}`);
