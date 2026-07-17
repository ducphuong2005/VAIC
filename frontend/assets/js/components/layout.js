import { icon } from './icons.js';
import { navItems, pageMeta } from '../data.js';

export function sidebar(page) {
  return `<aside class="sidebar" id="sidebar">
    <div class="brand"><span class="brand-mark">${icon('discover',27)}</span><span>CAREER<br>COMPASS</span></div>
    <nav class="nav-list">${navItems.map(([id,label,url,ico],i)=>`${i===7?'<span class="nav-divider"></span>':''}<a class="nav-item ${page===id?'active':''}" href="${url}">${icon(ico,19)}<span>${label}</span></a>`).join('')}</nav>
    <div class="profile-progress">
      <div class="ring"><div><small>Hồ sơ của bạn</small><strong>72%</strong></div></div>
      <p>Hoàn thành hồ sơ để nhận<br>gợi ý chính xác hơn nhé!</p>
      <a class="btn btn-glass btn-sm" href="profile.html">Tiếp tục hoàn thiện</a>
    </div>
  </aside><div class="sidebar-overlay" data-sidebar-close></div>`;
}

export function header(page) {
  const [title,subtitle] = pageMeta[page];
  return `<header class="topbar">
    <div class="heading"><button class="icon-btn mobile-menu" data-sidebar-open aria-label="Mở menu">${icon('menu')}</button><div><h1>${title}</h1><p>${subtitle}</p></div></div>
    <div class="top-actions"><button class="icon-btn notification" aria-label="Thông báo">${icon('bell')}<span></span></button><button class="user-menu"><span class="avatar">MA</span><span>Minh Anh</span><span class="caret">⌄</span></button></div>
  </header>`;
}

export function layout(page, content) {
  return `${sidebar(page)}<main class="main-shell">${header(page)}<section class="page-content page-${page}">${content}</section><footer>Career Compass © 2026 · Đồng hành cùng sự nghiệp của bạn</footer></main><div id="toast" class="toast"></div>`;
}

export function card(title, content, options={}) {
  const ico=options.icon?`<span class="title-icon ${options.tone||''}">${icon(options.icon,19)}</span>`:'';
  const head=(title||options.action)?`<div class="card-head"><h2>${ico}${title}</h2>${options.action||''}</div>`:'';
  return `<section class="card ${options.className||''}">${head}${content}</section>`;
}

export function emptyState(iconName,title,text,action='') {
  return `<div class="empty-state"><span>${icon(iconName,38)}</span><h3>${title}</h3><p>${text}</p>${action}</div>`;
}
