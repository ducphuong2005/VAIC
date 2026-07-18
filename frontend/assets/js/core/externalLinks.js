import { escapeHtml } from './formatters.js';

function normalizeText(value = '') {
  return String(value)
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'd')
    .trim();
}

function slugify(value = '') {
  return normalizeText(value)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '') || 'viec-lam';
}

export function jobSearchLinks(query = '') {
  const q = String(query || 'viec lam').trim() || 'viec lam';
  return [
    {
      provider: 'TopCV',
      title: `Tìm ${q} trên TopCV`,
      url: `https://www.topcv.vn/tim-viec-lam-${slugify(q)}`
    },
    {
      provider: 'JobsGO',
      title: `Tìm ${q} trên JobsGO`,
      url: `https://jobsgo.vn/viec-lam.html?keyword=${encodeURIComponent(q)}`
    }
  ];
}

export function renderJobSearchLinks(query = '', description = '') {
  return `<div class="external-links">${jobSearchLinks(query).map(link => `
    <div class="external-link">
      <a href="${escapeHtml(link.url)}" target="_blank" rel="noreferrer">${escapeHtml(link.provider)}</a>
      <button class="save-link-btn" type="button"
        data-save-link
        data-link-type="JOB"
        data-provider="${escapeHtml(link.provider)}"
        data-title="${escapeHtml(link.title)}"
        data-url="${escapeHtml(link.url)}"
        data-description="${escapeHtml(description || query)}">Lưu</button>
    </div>`).join('')}</div>`;
}
