// Lightweight API helper — uses relative paths, proxied to localhost:8080 by CRA dev server
async function tryFetch(url, options) {
  const res = await fetch(url, options);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    let message = `HTTP ${res.status}`;
    if (text.trim().startsWith('<')) {
      message = 'Cannot connect to server. Make sure the backend is running on port 8080.';
    } else if (text) {
      try {
        const json = JSON.parse(text);
        message = (json.error && json.error.message) || json.message || text;
      } catch {
        message = text;
      }
    }
    const err = new Error(message);
    err.status = res.status;
    throw err;
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return res.json();
  return res.text();
}

async function request(method, path, body, opts = {}) {
  if (!path) path = '/';
  if (!path.startsWith('/')) path = `/${path}`;
  const headers = Object.assign({ 'Accept': 'application/json' }, opts.headers || {});
  const init = { method, headers, credentials: opts.credentials || 'same-origin' };
  if (body != null) {
    if (body instanceof FormData) {
      delete headers['Content-Type'];
      init.body = body;
    } else {
      headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(body);
    }
  }
  return tryFetch(path, init);
}

export const apiGet = (path, opts) => request('GET', path, null, opts);
export const apiPost = (path, body, opts) => request('POST', path, body, opts);
export const apiPut = (path, body, opts) => request('PUT', path, body, opts);
export const apiDelete = (path, body, opts) => request('DELETE', path, body, opts);
