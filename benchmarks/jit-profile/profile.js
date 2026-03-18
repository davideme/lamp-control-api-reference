/**
 * JIT Warmup Profile — k6 script for a single 30-second measurement window.
 *
 * All 6 language services are tested in parallel via separate scenarios.
 * Each request is tagged with { language: <lang> } so k6 tracks per-language
 * latency distributions. The `thresholds` trick forces those tagged sub-metrics
 * into the handleSummary output.
 *
 * Usage (called by run-profile.sh, not directly):
 *   k6 run \
 *     --env TS_URL=https://... --env PY_URL=https://... --env GO_URL=https://... \
 *     --env JAVA_URL=https://... --env KT_URL=https://... --env CS_URL=https://... \
 *     --env TARGET_RPS=50 --env WINDOW=0 \
 *     profile.js
 */

import http from 'k6/http';
import { check } from 'k6';

const TARGET_RPS = Number(__ENV.TARGET_RPS || 50);
const WINDOW = __ENV.WINDOW || '0';
const WINDOW_DURATION = __ENV.WINDOW_DURATION || '30s';
const WINDOW_SECONDS = parseInt(WINDOW_DURATION, 10); // "30s" → 30
const RESULTS_DIR_ENV = __ENV.RESULTS_DIR || 'results';

// Env var names aligned with benchmarks/k6/scenarios.js; legacy aliases supported.
const SEED_FETCH_PAGES = Number(__ENV.SEED_FETCH_PAGES || __ENV.SEED_PAGES || 3);
const SEED_PAGE_SIZE   = Number(__ENV.SEED_PAGE_SIZE   || __ENV.PAGE_SIZE  || 25);

const SERVICES = {
  typescript: (__ENV.TS_URL || '').replace(/\/$/, ''),
  python: (__ENV.PY_URL || '').replace(/\/$/, ''),
  go: (__ENV.GO_URL || '').replace(/\/$/, ''),
  java: (__ENV.JAVA_URL || '').replace(/\/$/, ''),
  kotlin: (__ENV.KT_URL || '').replace(/\/$/, ''),
  csharp: (__ENV.CS_URL || '').replace(/\/$/, ''),
};

const ACTIVE_SERVICES = Object.fromEntries(
  Object.entries(SERVICES).filter(([, url]) => url !== '')
);

const PRE_ALLOC = Math.max(10, TARGET_RPS * 2);
const MAX_VUS = Math.max(50, TARGET_RPS * 4);

function makeScenario(lang, baseUrl) {
  return {
    executor: 'constant-arrival-rate',
    rate: TARGET_RPS,
    timeUnit: '1s',
    duration: WINDOW_DURATION,
    preAllocatedVUs: PRE_ALLOC,
    maxVUs: MAX_VUS,
    exec: 'runCRUD',
    env: { LANG: lang, BASE_URL: baseUrl },
  };
}

export const options = {
  discardResponseBodies: false,
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)'],
  scenarios: Object.fromEntries(
    Object.entries(ACTIVE_SERVICES).map(([lang, url]) => [lang, makeScenario(lang, url)])
  ),
  // Empty thresholds force k6 to track each tagged sub-metric in handleSummary output.
  thresholds: Object.fromEntries(
    Object.keys(ACTIVE_SERVICES).map((lang) => [`http_req_duration{language:${lang}}`, []])
  ),
};

// ─── Seed setup ──────────────────────────────────────────────────────────────

export function setup() {
  const seedData = {};
  for (const [lang, baseUrl] of Object.entries(ACTIVE_SERVICES)) {
    seedData[lang] = fetchSeedIds(baseUrl);
  }
  return seedData;
}

function fetchSeedIds(baseUrl) {
  const ids = [];
  let cursor = null;

  for (let i = 0; i < SEED_FETCH_PAGES; i++) {
    const query = cursor
      ? `/v1/lamps?pageSize=${SEED_PAGE_SIZE}&cursor=${encodeURIComponent(cursor)}`
      : `/v1/lamps?pageSize=${SEED_PAGE_SIZE}`;

    const resp = http.get(`${baseUrl}${query}`);
    if (resp.status !== 200) break;

    let body;
    try { body = resp.json(); } catch (_) { break; }
    if (!body || !Array.isArray(body.data)) break;

    for (const lamp of body.data) {
      if (lamp && typeof lamp.id === 'string') ids.push(lamp.id);
    }

    if (!body.hasMore || !body.nextCursor) break;
    cursor = body.nextCursor;
  }

  return ids;
}

// ─── Per-VU state ─────────────────────────────────────────────────────────────
// Each VU gets its own copy of this array (k6 isolates VU module state).
let vuOwnedIds = [];

// ─── CRUD operations ──────────────────────────────────────────────────────────

function doList(baseUrl, tags) {
  const resp = http.get(`${baseUrl}/v1/lamps?pageSize=${SEED_PAGE_SIZE}`, { tags });
  check(resp, { 'list 200': (r) => r.status === 200 });
}

function doCreate(baseUrl, tags) {
  const payload = JSON.stringify({ status: Math.random() < 0.5 });
  const resp = http.post(`${baseUrl}/v1/lamps`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags,
  });
  const ok = check(resp, { 'create 201': (r) => r.status === 201 });
  if (ok) {
    let body;
    try { body = resp.json(); } catch (_) { return; }
    if (body && typeof body.id === 'string') vuOwnedIds.push(body.id);
  }
}

function doGet(baseUrl, lampId, tags) {
  const resp = http.get(`${baseUrl}/v1/lamps/${lampId}`, { tags });
  check(resp, { 'get 200': (r) => r.status === 200 });
}

function doUpdate(baseUrl, lampId, tags) {
  const payload = JSON.stringify({ status: Math.random() < 0.5 });
  const resp = http.put(`${baseUrl}/v1/lamps/${lampId}`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags,
  });
  check(resp, { 'update 200': (r) => r.status === 200 });
}

function doDelete(baseUrl, lampId, tags) {
  const resp = http.del(`${baseUrl}/v1/lamps/${lampId}`, null, { tags });
  check(resp, { 'delete 204': (r) => r.status === 204 });
}

function pickId(seedIds) {
  if (vuOwnedIds.length > 0) return vuOwnedIds[Math.floor(Math.random() * vuOwnedIds.length)];
  if (seedIds.length > 0) return seedIds[Math.floor(Math.random() * seedIds.length)];
  return null;
}

// Weights matching the existing benchmark config (50/20/20/7/3).
const WEIGHTS = { list: 50, get: 20, create: 20, update: 7, delete: 3 };
const TOTAL_WEIGHT = Object.values(WEIGHTS).reduce((a, b) => a + b, 0);

function pickOperation() {
  const r = Math.random() * TOTAL_WEIGHT;
  if (r < WEIGHTS.list) return 'list';
  if (r < WEIGHTS.list + WEIGHTS.get) return 'get';
  if (r < WEIGHTS.list + WEIGHTS.get + WEIGHTS.create) return 'create';
  if (r < WEIGHTS.list + WEIGHTS.get + WEIGHTS.create + WEIGHTS.update) return 'update';
  return 'delete';
}

// ─── Main VU function (all 6 scenarios share this exec target) ────────────────

export function runCRUD(data) {
  const lang = __ENV.LANG;
  const baseUrl = __ENV.BASE_URL;
  const seedIds = (data && data[lang]) ? data[lang] : [];
  const tags = { language: lang };

  const op = pickOperation();

  if (op === 'list') {
    doList(baseUrl, tags);
  } else if (op === 'create') {
    doCreate(baseUrl, tags);
  } else if (op === 'get') {
    const id = pickId(seedIds);
    if (id) doGet(baseUrl, id, tags);
    else doCreate(baseUrl, tags);
  } else if (op === 'update') {
    const id = pickId(seedIds);
    if (id) doUpdate(baseUrl, id, tags);
    else doCreate(baseUrl, tags);
  } else {
    // delete: prefer VU-owned so we don't exhaust seeded IDs
    const id = vuOwnedIds.length > 0 ? vuOwnedIds.pop() : null;
    if (id) doDelete(baseUrl, id, tags);
    else doCreate(baseUrl, tags);
  }
}

// ─── Summary export ───────────────────────────────────────────────────────────

export function handleSummary(data) {
  const windowIdx = parseInt(WINDOW, 10);
  const elapsedSeconds = windowIdx * WINDOW_SECONDS;

  const metrics = {};
  for (const lang of Object.keys(ACTIVE_SERVICES)) {
    const key = `http_req_duration{language:${lang}}`;
    const m = data.metrics[key];
    if (!m || !m.values) {
      metrics[lang] = null;
      continue;
    }
    metrics[lang] = {
      p50: m.values['med'] !== undefined ? Math.round(m.values['med'] * 100) / 100 : null,
      p95: m.values['p(95)'] !== undefined ? Math.round(m.values['p(95)'] * 100) / 100 : null,
      p99: m.values['p(99)'] !== undefined ? Math.round(m.values['p(99)'] * 100) / 100 : null,
      avg: m.values['avg'] !== undefined ? Math.round(m.values['avg'] * 100) / 100 : null,
    };
  }

  const output = {
    window: windowIdx,
    elapsed_seconds: elapsedSeconds,
    target_rps: TARGET_RPS,
    metrics,
  };

  return {
    [`${RESULTS_DIR_ENV}/window_${WINDOW}.json`]: JSON.stringify(output, null, 2),
    stdout: `\n[window ${WINDOW}] t=${elapsedSeconds}s complete\n`,
  };
}
