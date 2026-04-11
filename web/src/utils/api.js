// Lightweight API helper that tries relative /api first then localhost:8080 fallback
async function tryFetch(url, options) {
  const res = await fetch(url, options);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    const err = new Error(text || `HTTP ${res.status}`);
    err.status = res.status;
    throw err;
  }
  // try parse json, fallback to text
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return res.json();
  return res.text();
}

const baseBackend = 'http://localhost:8080';

function buildUrls(path) {
  if (!path) path = '/';
  if (!path.startsWith('/')) path = `/${path}`;
  // Try backend first, then relative path as fallback
  return [`${baseBackend}${path}`, path];
}

async function request(method, path, body, opts = {}) {
  const urls = buildUrls(path);
  const headers = Object.assign({ 'Accept': 'application/json' }, opts.headers || {});
  const init = {
    method,
    headers,
    credentials: opts.credentials || 'same-origin',
  };
  if (body != null) {
    if (body instanceof FormData) {
      // let browser set content-type for FormData
      delete headers['Content-Type'];
      init.body = body;
    } else {
      headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(body);
    }
  }

  let lastErr = null;
  for (const url of urls) {
    try {
      return await tryFetch(url, init);
    } catch (err) {
      lastErr = err;
      // continue to next url
    }
  }

  throw lastErr || new Error('Failed to fetch');
}

export const apiGet = (path, opts) => request('GET', path, null, opts);
export const apiPost = (path, body, opts) => request('POST', path, body, opts);
export const apiPut = (path, body, opts) => request('PUT', path, body, opts);
export const apiDelete = (path, body, opts) => request('DELETE', path, body, opts);
