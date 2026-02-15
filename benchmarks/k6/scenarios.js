import http from 'k6/http';
import { check } from 'k6';
import { sleep } from 'k6';
import { Gauge, Rate, Trend } from 'k6/metrics';

const RUN_MODE = (__ENV.RUN_MODE || 'fixed').trim();
const BASE_URL = (__ENV.BASE_URL || '').replace(/\/$/, '');
const BASE_PATH = __ENV.BASE_PATH || '/v1';
const TARGET_RPS = Number(__ENV.TARGET_RPS || 1);
const DURATION = __ENV.DURATION || '60s';
const PAGE_SIZE = Number(__ENV.PAGE_SIZE || 25);
const SEED_FETCH_PAGES = Number(__ENV.SEED_FETCH_PAGES || 10);
const SEED_PAGE_SIZE = Number(__ENV.SEED_PAGE_SIZE || 100);
const AUTH_HEADER = __ENV.AUTH_HEADER || '';
const COLD_START_ENDPOINT = __ENV.COLD_START_ENDPOINT || '/lamps?pageSize=1';
const COLD_START_SUCCESS_STATUS = Number(__ENV.COLD_START_SUCCESS_STATUS || 200);
const COLD_START_MAX_WAIT_SECONDS = Number(__ENV.COLD_START_MAX_WAIT_SECONDS || 60);
const COLD_START_PROBE_INTERVAL_MS = Number(__ENV.COLD_START_PROBE_INTERVAL_MS || 500);

const LIST_WEIGHT = Number(__ENV.LIST_WEIGHT || 50);
const GET_WEIGHT = Number(__ENV.GET_WEIGHT || 20);
const CREATE_WEIGHT = Number(__ENV.CREATE_WEIGHT || 20);
const UPDATE_WEIGHT = Number(__ENV.UPDATE_WEIGHT || 7);
const DELETE_WEIGHT = Number(__ENV.DELETE_WEIGHT || 3);

const PRE_ALLOCATED_VUS = Number(
  __ENV.PRE_ALLOCATED_VUS || Math.max(10, Math.ceil(TARGET_RPS * 2))
);
const MAX_VUS = Number(__ENV.MAX_VUS || Math.max(50, Math.ceil(TARGET_RPS * 4)));

if (!BASE_URL) {
  throw new Error('BASE_URL is required');
}

const requestDuration = new Trend(`${RUN_MODE}_req_duration`, true);
const errorRate = new Rate(`${RUN_MODE}_error_rate`);
const coldStartReadyMs = new Trend('cold_start_ready_ms', true);
const coldStartAttempts = new Gauge('cold_start_attempts');
const coldStartFirstSuccessStatus = new Gauge('cold_start_first_success_status');

export const options = {
  discardResponseBodies: false,
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  scenarios: RUN_MODE === 'cold_start'
    ? {
      main: {
        executor: 'per-vu-iterations',
        vus: 1,
        iterations: 1,
        maxDuration: `${Math.max(10, COLD_START_MAX_WAIT_SECONDS + 10)}s`,
      },
    }
    : {
      main: {
        executor: 'constant-arrival-rate',
        rate: TARGET_RPS,
        timeUnit: '1s',
        duration: DURATION,
        preAllocatedVUs: PRE_ALLOCATED_VUS,
        maxVUs: MAX_VUS,
      },
    },
};

const headers = {
  'Content-Type': 'application/json',
};

if (AUTH_HEADER) {
  headers.Authorization = AUTH_HEADER;
}

let vuOwnedIds = [];

function url(pathAndQuery) {
  return `${BASE_URL}${BASE_PATH}${pathAndQuery}`;
}

function parseJson(resp) {
  try {
    return resp.json();
  } catch (_err) {
    return null;
  }
}

function track(resp, ok) {
  requestDuration.add(resp.timings.duration);
  errorRate.add(!ok);
}

function req(method, endpoint, body, expectedStatuses) {
  const response = http.request(method, url(endpoint), body, { headers });
  const ok = check(response, {
    [`${method} ${endpoint} status`]: (r) => expectedStatuses.includes(r.status),
  });
  track(response, ok);
  return { response, ok };
}

function randomBool() {
  return Math.random() < 0.5;
}

function randomFrom(list) {
  return list[Math.floor(Math.random() * list.length)];
}

function listLamps() {
  const { response } = req('GET', `/lamps?pageSize=${PAGE_SIZE}`, null, [200]);
  const body = parseJson(response);
  check(body, {
    'list lamps has data array': (b) => b && Array.isArray(b.data),
    'list lamps has hasMore': (b) => b && typeof b.hasMore === 'boolean',
  });
}

function createLamp() {
  const payload = JSON.stringify({ status: randomBool() });
  const { response, ok } = req('POST', '/lamps', payload, [201]);
  if (!ok) {
    return;
  }

  const body = parseJson(response);
  const hasId = check(body, {
    'create lamp has id': (b) => b && typeof b.id === 'string' && b.id.length > 0,
    'create lamp has status': (b) => b && typeof b.status === 'boolean',
  });

  if (hasId) {
    vuOwnedIds.push(body.id);
  }
}

function pickLampId(data) {
  if (vuOwnedIds.length > 0) {
    return randomFrom(vuOwnedIds);
  }
  if (data.seedIds.length > 0) {
    return randomFrom(data.seedIds);
  }
  return null;
}

function getLamp(data) {
  let lampId = pickLampId(data);
  if (!lampId) {
    createLamp();
    lampId = vuOwnedIds[vuOwnedIds.length - 1] || null;
  }
  if (!lampId) {
    return;
  }

  const { response } = req('GET', `/lamps/${lampId}`, null, [200]);
  const body = parseJson(response);
  check(body, {
    'get lamp has id': (b) => b && typeof b.id === 'string',
    'get lamp has status': (b) => b && typeof b.status === 'boolean',
  });
}

function updateLamp(data) {
  let lampId = pickLampId(data);
  if (!lampId) {
    createLamp();
    lampId = vuOwnedIds[vuOwnedIds.length - 1] || null;
  }
  if (!lampId) {
    return;
  }

  const payload = JSON.stringify({ status: randomBool() });
  const { response } = req('PUT', `/lamps/${lampId}`, payload, [200]);
  const body = parseJson(response);
  check(body, {
    'update lamp has id': (b) => b && typeof b.id === 'string',
    'update lamp has status': (b) => b && typeof b.status === 'boolean',
  });
}

function deleteLamp() {
  let lampId = null;

  if (vuOwnedIds.length > 0) {
    lampId = vuOwnedIds.pop();
  } else {
    const payload = JSON.stringify({ status: randomBool() });
    const { response, ok } = req('POST', '/lamps', payload, [201]);
    if (!ok) {
      return;
    }
    const body = parseJson(response);
    if (!body || typeof body.id !== 'string') {
      return;
    }
    lampId = body.id;
  }

  if (!lampId) {
    return;
  }

  req('DELETE', `/lamps/${lampId}`, null, [204]);
}

function pickOperation() {
  const total = LIST_WEIGHT + GET_WEIGHT + CREATE_WEIGHT + UPDATE_WEIGHT + DELETE_WEIGHT;
  const pick = Math.random() * total;

  if (pick < LIST_WEIGHT) {
    return 'list';
  }
  if (pick < LIST_WEIGHT + GET_WEIGHT) {
    return 'get';
  }
  if (pick < LIST_WEIGHT + GET_WEIGHT + CREATE_WEIGHT) {
    return 'create';
  }
  if (pick < LIST_WEIGHT + GET_WEIGHT + CREATE_WEIGHT + UPDATE_WEIGHT) {
    return 'update';
  }
  return 'delete';
}

export function setup() {
  if (RUN_MODE === 'cold_start') {
    return { seedIds: [] };
  }

  const seedIds = [];
  let cursor = null;

  for (let i = 0; i < SEED_FETCH_PAGES; i += 1) {
    const query = cursor
      ? `/lamps?pageSize=${SEED_PAGE_SIZE}&cursor=${encodeURIComponent(cursor)}`
      : `/lamps?pageSize=${SEED_PAGE_SIZE}`;

    const response = http.get(url(query), { headers });
    const ok = check(response, {
      'seed fetch status 200': (r) => r.status === 200,
    });

    if (!ok) {
      break;
    }

    const body = parseJson(response);
    if (!body || !Array.isArray(body.data)) {
      break;
    }

    for (const lamp of body.data) {
      if (lamp && typeof lamp.id === 'string') {
        seedIds.push(lamp.id);
      }
    }

    if (!body.hasMore || !body.nextCursor) {
      break;
    }

    cursor = body.nextCursor;
  }

  return { seedIds };
}

function runColdStartProbe() {
  const startedAtMs = Date.now();
  const deadlineMs = startedAtMs + Math.max(1000, COLD_START_MAX_WAIT_SECONDS * 1000);
  let attempts = 0;

  while (Date.now() <= deadlineMs) {
    const response = http.get(url(COLD_START_ENDPOINT), { headers });
    attempts += 1;

    const ok = response.status === COLD_START_SUCCESS_STATUS;
    requestDuration.add(response.timings.duration);
    errorRate.add(!ok);

    if (ok) {
      const readyMs = Date.now() - startedAtMs;
      coldStartReadyMs.add(readyMs);
      coldStartAttempts.add(attempts);
      coldStartFirstSuccessStatus.add(response.status);
      return;
    }

    sleep(Math.max(10, COLD_START_PROBE_INTERVAL_MS) / 1000);
  }

  coldStartAttempts.add(attempts);
  coldStartFirstSuccessStatus.add(0);
}

export default function (data) {
  if (RUN_MODE === 'cold_start') {
    runColdStartProbe();
    return;
  }

  const operation = pickOperation();

  if (operation === 'list') {
    listLamps();
  } else if (operation === 'get') {
    getLamp(data);
  } else if (operation === 'create') {
    createLamp();
  } else if (operation === 'update') {
    updateLamp(data);
  } else {
    deleteLamp();
  }
}
