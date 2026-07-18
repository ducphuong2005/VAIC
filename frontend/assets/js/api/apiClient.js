import { API_BASE_URL, API_TIMEOUT_MS } from '../core/config.js';
import {
  clearTokens,
  getAccessToken,
  getCurrentUser,
  getRefreshToken,
  setCurrentUser,
  setTokens
} from '../core/authStore.js';
import { ApiError } from '../core/errors.js';

const pending = new Map();
let refreshing;
let demoAuthenticating;

const demoUser = {
  email: 'demo@career-compass.local',
  password: 'DemoPass123',
  fullName: 'Career Compass Demo'
};

export const normalizeApiResponse = payload =>
  payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;

async function refresh() {
  if (refreshing) return refreshing;
  const token = getRefreshToken();
  if (!token) return false;

  refreshing = fetch(`${API_BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ refreshToken: token })
  })
    .then(async response => {
      if (!response.ok) return false;
      const data = normalizeApiResponse(await response.json());
      if (!data?.accessToken) return false;
      setTokens(data);
      return true;
    })
    .catch(() => false)
    .finally(() => {
      refreshing = null;
    });

  return refreshing;
}

async function postAuth(path, body) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body)
  });
  const payload = (response.headers.get('content-type') || '').includes('json')
    ? await response.json()
    : await response.text();
  if (!response.ok) {
    throw new ApiError(payload?.message || 'Không thể xác thực tài khoản demo.', { status: response.status });
  }
  return normalizeApiResponse(payload);
}

async function authenticateDemo() {
  if (demoAuthenticating) return demoAuthenticating;

  demoAuthenticating = (async () => {
    let auth;
    try {
      auth = await postAuth('/auth/register', demoUser);
    } catch (error) {
      if (error?.status !== 409) throw error;
      auth = await postAuth('/auth/login', {
        email: demoUser.email,
        password: demoUser.password
      });
    }
    setTokens(auth);
    setCurrentUser(auth.user || null);
    return true;
  })().finally(() => {
    demoAuthenticating = null;
  });

  return demoAuthenticating;
}

function isAuthPath(path) {
  return path.startsWith('/auth/') || path.startsWith('auth/');
}

function shouldRecoverDemoSession() {
  return getCurrentUser()?.email === demoUser.email;
}

async function recoverAuthentication(path, options, status, retry) {
  if (!options.auth || !retry || isAuthPath(path) || (status !== 401 && status !== 403)) {
    return false;
  }
  if (status === 401 && await refresh()) {
    return true;
  }
  if (shouldRecoverDemoSession()) {
    return authenticateDemo();
  }
  clearTokens();
  return false;
}

async function readPayload(response) {
  if (response.status === 204) return null;
  return (response.headers.get('content-type') || '').includes('json')
    ? response.json()
    : response.text();
}

async function execute(path, options, retry = true) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), options.timeout);
  const abort = () => controller.abort();
  options.signal?.addEventListener('abort', abort, { once: true });

  try {
    const token = options.auth && getAccessToken();
    const response = await fetch(`${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`, {
      method: options.method,
      signal: controller.signal,
      headers: {
        Accept: 'application/json',
        ...(options.body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers
      },
      body: options.body === undefined ? undefined : JSON.stringify(options.body)
    });
    const payload = await readPayload(response);

    if (!response.ok) {
      if (await recoverAuthentication(path, options, response.status, retry)) {
        return execute(path, options, false);
      }
      throw new ApiError(payload?.message || 'Yêu cầu không thành công.', {
        status: response.status,
        validationErrors: payload?.errors || {}
      });
    }

    return normalizeApiResponse(payload);
  } catch (error) {
    if (error instanceof ApiError) throw error;
    if (controller.signal.aborted && !options.signal?.aborted) {
      throw new ApiError('Yêu cầu đã hết thời gian chờ.', { code: 'TIMEOUT', cause: error });
    }
    throw new ApiError('Không kết nối được backend. Kiểm tra backend và cổng API.', {
      code: 'NETWORK',
      cause: error
    });
  } finally {
    clearTimeout(timer);
    options.signal?.removeEventListener('abort', abort);
  }
}

export function request(path, {
  method = 'GET',
  body,
  auth = true,
  signal,
  headers = {},
  timeout = API_TIMEOUT_MS
} = {}) {
  const options = { method: method.toUpperCase(), body, auth, signal, headers, timeout };
  const key = options.method === 'GET' ? null : `${options.method}:${path}:${JSON.stringify(body ?? null)}`;
  if (key && pending.has(key)) return pending.get(key);

  const promise = execute(path, options);
  if (key) {
    pending.set(key, promise);
    promise.finally(() => pending.delete(key));
  }
  return promise;
}

export const get = (path, options) => request(path, { ...options, method: 'GET' });
export const post = (path, body, options) => request(path, { ...options, method: 'POST', body });
export const put = (path, body, options) => request(path, { ...options, method: 'PUT', body });
export const del = (path, options) => request(path, { ...options, method: 'DELETE' });
