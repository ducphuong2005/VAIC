import { icon } from './icons.js';
import { navItems, pageMeta } from '../data.js';
import { clearTokens, getCurrentUser, getDisplayName, getUserInitials, isAuthenticated } from '../core/authStore.js';

export function sidebar(page) {
  const profileBox = isAuthenticated()
    ? `<div class="profile-progress sidebar-status"><span class="status-icon">${icon('user', 22)}</span><small>Hồ sơ backend</small><p>Cập nhật hồ sơ để backend tính gợi ý chính xác hơn.</p><a class="btn btn-glass btn-sm" href="profile.html">Mở hồ sơ</a></div>`
    : `<div class="profile-progress sidebar-status"><span class="status-icon">${icon('lock', 22)}</span><small>Chưa đăng nhập</small><p>Đăng nhập để xem hồ sơ và dữ liệu cá nhân từ backend.</p><a class="btn btn-glass btn-sm" href="login.html">Đăng nhập</a></div>`;
  return `<aside class="sidebar" id="sidebar">
    <div class="brand"><span class="brand-mark">${icon('discover', 27)}</span><span>CAREER<br>COMPASS</span></div>
    <nav class="nav-list">${navItems.map(([id, label, url, ico], i) => `${i === 7 ? '<span class="nav-divider"></span>' : ''}<a class="nav-item ${page === id ? 'active' : ''}" href="${url}">${icon(ico, 19)}<span>${label}</span></a>`).join('')}</nav>
    ${profileBox}
  </aside><div class="sidebar-overlay" data-sidebar-close></div>`;
}

export function header(page) {
  const user = getCurrentUser(), name = getDisplayName(user), initials = getUserInitials(user), signedIn = isAuthenticated();
  const meta = typeof pageMeta[page] === 'function' ? pageMeta[page](user) : pageMeta[page];
  const [title, subtitle] = meta;
  const account = signedIn
    ? `<a class="user-menu" href="profile.html" title="Hồ sơ của bạn"><span class="avatar">${initials}</span><span>${name}</span><span class="caret">⌄</span></a>`
    : `<a class="user-menu" href="login.html"><span class="avatar">${icon('user', 17)}</span><span>Đăng nhập</span></a>`;
  return `<header class="topbar">
    <div class="heading"><button class="icon-btn mobile-menu" data-sidebar-open aria-label="Mở menu">${icon('menu')}</button><div><h1>${title}</h1><p>${subtitle}</p></div></div>
    <div class="top-actions"><button class="icon-btn notification" aria-label="Thông báo">${icon('bell')}<span></span></button>${account}</div>
  </header>`;
}

export function layout(page, content) {
  return `${sidebar(page)}<main class="main-shell">${header(page)}<section class="page-content page-${page}">${content}</section><footer>Career Compass © 2026 · Đồng hành cùng sự nghiệp của bạn</footer></main><div id="toast" class="toast"></div>`;
}

export function bindLayoutEvents() {
  document.querySelector('[data-logout]')?.addEventListener('click', () => {
    clearTokens();
    location.href = 'login.html';
  });
}

export function card(title, content, options = {}) {
  const ico = options.icon ? `<span class="title-icon ${options.tone || ''}">${icon(options.icon, 19)}</span>` : '';
  const head = (title || options.action) ? `<div class="card-head"><h2>${ico}${title}</h2>${options.action || ''}</div>` : '';
  return `<section class="card ${options.className || ''}">${head}${content}</section>`;
}

export function emptyState(iconName, title, text, action = '') {
  return `<div class="empty-state"><span>${icon(iconName, 38)}</span><h3>${title}</h3><p>${text}</p>${action}</div>`;
}
