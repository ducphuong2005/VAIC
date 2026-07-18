import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { getLearningPaths } from '../api/learningApi.js';
import { isAuthenticated } from '../core/authStore.js';
import { escapeHtml } from '../core/formatters.js';

export async function learningPage() {
  if (!isAuthenticated()) throw new Error('Bạn cần đăng nhập để tải lộ trình học tập từ backend.');
  const paths = await getLearningPaths();
  const path = paths?.[0];
  if (!path) return '<div class="empty-state"><h3>Chưa có lộ trình</h3><p>Backend chưa tạo lộ trình học tập cho tài khoản này.</p><a class="btn btn-primary" href="recommendations.html">Tạo gợi ý nghề trước</a></div>';
  const steps = path.steps || [];
  const avg = steps.length ? Math.round(steps.reduce((sum, step) => sum + (step.progressPercent || 0), 0) / steps.length) : 0;
  const overview = `<div class="learning-overview"><div><span class="path-badge">LỘ TRÌNH TỪ BACKEND</span><h2>${escapeHtml(path.title)}</h2><p>${escapeHtml(path.route || path.onetCode || 'Career Compass')}</p><div class="path-meta"><span>${escapeHtml(path.status || 'ACTIVE')}</span><span>${steps.length} bước học</span></div></div><div class="overall-progress"><div class="big-ring small"><strong>${avg}%</strong><span>Hoàn thành</span></div><button class="btn btn-white">Tiếp tục học ${icon('play', 15)}</button></div></div>`;
  const timeline = `<div class="learning-timeline">${steps.map((step, i) => `<article class="module ${!step.completed && i > 0 && !steps[i - 1]?.completed ? 'locked' : ''}"><div class="module-number">${step.completed ? '✓' : step.stepOrder || i + 1}</div><div class="module-card"><div class="module-icon">${icon(step.completed ? 'check' : 'route', 20)}</div><div class="module-info"><span>CHẶNG ${step.stepOrder || i + 1}</span><h3>${escapeHtml(step.title)}</h3><p>${escapeHtml(step.description || 'Backend chưa có mô tả.')}</p><div class="module-meta"><span>${step.durationHours || 0} giờ</span><span>${escapeHtml(step.targetSkill || 'Kỹ năng')}</span></div></div><div class="module-action"><strong>${step.progressPercent || 0}%</strong><div class="progress slim"><i style="width:${step.progressPercent || 0}%"></i></div></div></div></article>`).join('')}</div>`;
  const side = `${card('Trạng thái backend', `<div class="weekly-goal"><strong>${avg}%</strong><span>${escapeHtml(path.status || 'ACTIVE')}</span><div class="progress"><i style="width:${avg}%"></i></div></div>`, { icon: 'trophy' })}${card('Kỹ năng sẽ đạt được', `<div class="learn-skills">${[...new Set(steps.map(x => x.targetSkill).filter(Boolean))].map(skill => `<span>${escapeHtml(skill)}</span>`).join('') || '<span>Backend chưa có kỹ năng</span>'}</div>`, { icon: 'spark' })}`;
  return `${overview}<div class="learning-layout"><div><div class="section-heading"><h2>Các chặng học tập</h2><span>${steps.length} bước từ backend</span></div>${timeline}</div><aside>${side}</aside></div>`;
}
