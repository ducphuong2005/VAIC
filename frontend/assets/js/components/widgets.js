import { icon } from './icons.js';
import { escapeHtml } from '../core/formatters.js';
import { renderJobSearchLinks } from '../core/externalLinks.js';

const colors = ['#4f68ed', '#7551ee', '#21a985', '#f09a48', '#38a9b4', '#8b5cf6'];
const symbols = ['⌁', '◫', '✦', '◎'];

export function toCareerCard(career = {}, index = 0) {
  const title = career.titleVi || career.careerName || career.titleEn || career.title || 'Chưa có tên nghề';
  const subtitle = career.description || career.titleEn || career.careerCluster || 'Backend chưa có mô tả cho nghề này.';
  const cluster = career.careerCluster || career.group || 'Nghề nghiệp';
  const score = career.scores?.finalScore ?? career.confidence ?? career.match;
  const match = score == null ? null : Math.round(Number(score) <= 1 ? Number(score) * 100 : Number(score));
  return {
    onetCode: career.onetCode,
    rank: career.rank || index + 1,
    title,
    subtitle,
    salary: career.salary || 'Theo dữ liệu backend',
    growth: career.growth ?? career.jobCount ?? career.postingCount ?? 0,
    color: colors[index % colors.length],
    match,
    tags: [cluster, career.titleEn, career.jobZone ? `Job zone ${career.jobZone}` : null].filter(Boolean)
  };
}

export function toTrendingCareer(item = {}, index = 0) {
  return toCareerCard({
    ...item,
    description: item.source ? `Nguồn: ${item.source}${item.limitation ? ` · ${item.limitation}` : ''}` : item.titleEn,
    growth: item.jobCount
  }, index);
}

export function sparkline(color, idx = 0) {
  const variants = ['2,28 14,12 27,20 40,7 54,13 68,3', '2,27 14,16 27,22 40,11 53,17 68,4', '2,26 14,18 27,23 41,11 54,14 68,5'];
  return `<svg class="sparkline" viewBox="0 0 70 32"><defs><linearGradient id="g${idx}" x1="0" y1="0" x2="0" y2="1"><stop stop-color="${color}" stop-opacity=".25"/><stop offset="1" stop-color="${color}" stop-opacity="0"/></linearGradient></defs><polygon points="${variants[idx % 3]},68,32 2,32" fill="url(#g${idx})"/><polyline points="${variants[idx % 3]}" fill="none" stroke="${color}" stroke-width="2"/><circle cx="68" cy="${[3, 4, 5][idx % 3]}" r="2.5" fill="${color}"/></svg>`;
}

export function careerRows(items = [], limit = 5) {
  if (!items.length) return '<div class="empty-state"><h3>Chưa có dữ liệu thị trường</h3><p>Backend chưa trả về nhóm ngành đang thịnh hành.</p></div>';
  return items.slice(0, limit).map((raw, i) => {
    const c = raw.title ? raw : toCareerCard(raw, i);
    return `<div class="career-row">
      <span class="rank" style="--accent:${c.color}">${c.rank}</span><div class="career-info"><strong>${escapeHtml(c.title)}</strong><small>${escapeHtml(c.subtitle)}</small></div>
      ${sparkline(c.color, i)}<div class="salary"><strong>${escapeHtml(c.salary)}</strong><small>${Number(c.growth) ? `▲ ${escapeHtml(c.growth)}` : 'Backend'}</small></div>
    </div>`;
  }).join('');
}

export function skillStrip(skills = []) {
  if (!skills.length) return '<div class="empty-state"><h3>Chưa có dữ liệu kỹ năng</h3><p>Hãy hoàn thành hồ sơ hoặc mini-game để backend cập nhật điểm mạnh.</p></div>';
  return `<div class="skills-strip">${skills.map((skill, i) => {
    const name = skill.elementName || skill.skillName || skill.name || skill.targetSkill || 'Kỹ năng';
    const rawValue = skill.score ?? skill.confidence ?? skill.postingCount ?? skill.value ?? 0;
    const value = Math.max(0, Math.min(100, Math.round(Number(rawValue) <= 1 ? Number(rawValue) * 100 : Number(rawValue))));
    return `<div class="skill-mini"><span>${['◆', '●', '▲', '■', '✦'][i % 5]}</span><div><strong>${escapeHtml(name)}</strong><small>${value}%</small><div class="mini-progress"><i style="width:${value}%"></i></div></div></div>`;
  }).join('')}</div>`;
}

export function recommendationCard(recommendation, compact = false) {
  if (!recommendation) return '<div class="empty-state"><h3>Chưa có dữ liệu</h3><p>Hoàn thành mini-game để backend phân tích hướng nghề phù hợp.</p><a class="btn btn-primary" href="index.html">Về trang chủ</a></div>';
  const title = recommendation.careerName || recommendation.titleVi || 'Nghề nghiệp';
  const match = Math.round(Number(recommendation.scores?.finalScore ?? recommendation.confidence ?? 0) <= 1 ? Number(recommendation.scores?.finalScore ?? recommendation.confidence ?? 0) * 100 : Number(recommendation.scores?.finalScore ?? recommendation.confidence ?? 0));
  const tags = [recommendation.group, recommendation.onetCode, ...(recommendation.marketEvidence || []).slice(0, 1)].filter(Boolean);
  const reasons = (recommendation.reasons || recommendation.considerations || []).slice(0, 3);
  return `<div class="recommend-card ${compact ? 'compact' : ''}"><div class="recommend-top"><span class="best-label">Phù hợp nhất với bạn</span><span class="match">${match || 0}% phù hợp</span></div><h3>${escapeHtml(title)}</h3><div class="tags">${tags.map(x => `<span>${escapeHtml(x)}</span>`).join('')}</div><h4>Vì sao phù hợp?</h4><ul class="check-list">${reasons.length ? reasons.map(x => `<li>${icon('check', 16)} ${escapeHtml(x)}</li>`).join('') : `<li>${icon('check', 16)} Backend chưa trả về giải thích chi tiết.</li>`}</ul><a href="index.html" class="text-link">Về trang chủ ${icon('arrow', 15)}</a></div>`;
}

export function careerGrid(items = []) {
  if (!items.length) return '<div class="empty-state"><h3>Chưa có nghề nghiệp</h3><p>Backend chưa trả về dữ liệu nghề nghiệp.</p></div>';
  return `<div class="career-grid">${items.map((raw, i) => {
    const c = raw.title ? raw : toCareerCard(raw, i);
    return `<article class="career-card" data-career="${escapeHtml(c.title.toLowerCase())}" data-onet-code="${escapeHtml(c.onetCode || '')}"><div class="career-cover cover-${i % 4}"><span>${symbols[i % symbols.length]}</span><button class="save-btn" aria-label="Lưu nghề" data-save="${escapeHtml(c.onetCode || '')}">${icon('bookmark', 18)}</button></div><div class="career-body"><div class="match-line"><span>${escapeHtml(c.tags[0] || 'Nghề nghiệp')}</span><strong>${c.match == null ? 'Từ backend' : `${c.match}% phù hợp`}</strong></div><h3>${escapeHtml(c.title)}</h3><p>${escapeHtml(c.subtitle)}</p><div class="career-stats"><span>${escapeHtml(c.salary)}</span><span class="up">${Number(c.growth) ? `▲ ${escapeHtml(c.growth)}` : 'Backend'}</span></div>${renderJobSearchLinks(c.title, c.subtitle)}<a class="btn btn-soft btn-block" href="index.html">Về trang chủ ${icon('arrow', 15)}</a></div></article>`;
  }).join('')}</div>`;
}
