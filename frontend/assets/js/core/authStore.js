const keys = {
  access: 'careerCompass.accessToken',
  refresh: 'careerCompass.refreshToken',
  user: 'careerCompass.user'
};

const read = key => {
  try { return localStorage.getItem(key); } catch { return null; }
};

const write = (key, value) => {
  try { value == null ? localStorage.removeItem(key) : localStorage.setItem(key, value); } catch {}
};

export const getAccessToken = () => read(keys.access);
export const getRefreshToken = () => read(keys.refresh);

export function setTokens({ accessToken, refreshToken } = {}) {
  write(keys.access, accessToken || null);
  if (refreshToken !== undefined) write(keys.refresh, refreshToken || null);
}

export function clearTokens() {
  write(keys.access, null);
  write(keys.refresh, null);
  write(keys.user, null);
}

export const getCurrentUser = () => {
  try { return JSON.parse(read(keys.user)); } catch { return null; }
};

export const setCurrentUser = user => write(keys.user, user ? JSON.stringify(user) : null);
export const isAuthenticated = () => Boolean(getAccessToken());

export function getUserInitials(user = getCurrentUser()) {
  const name = (user?.fullName || user?.name || user?.email || 'Bạn').trim();
  const parts = name.split(/\s+/).filter(Boolean);
  if (!parts.length) return 'B';
  return parts.slice(-2).map(x => x[0]).join('').toUpperCase();
}

export function getDisplayName(user = getCurrentUser()) {
  return (user?.fullName || user?.name || 'Bạn').trim();
}

export function updateLocalUser(changes = {}) {
  const current = getCurrentUser();
  if (!current) return null;
  const next = { ...current, ...changes, email: String(changes.email || current.email || '').trim().toLowerCase() };
  setCurrentUser(next);
  return next;
}
