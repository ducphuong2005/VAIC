import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { recommendationCard } from '../components/widgets.js';
import { getDisplayName, isAuthenticated } from '../core/authStore.js';
import { generateRecommendations, getLatestRecommendations } from '../api/recommendationApi.js';
import { escapeHtml } from '../core/formatters.js';

let run = null;

export async function recommendationsPage() {
  if (!isAuthenticated()) throw new Error('Bạn cần đăng nhập để tải gợi ý từ backend.');
  try {
    run = await getLatestRecommendations();
  } catch {
    run = null;
  }
  const displayName = getDisplayName();
  const recommendations = run?.recommendations || [];
  const best = recommendations[0];
  const aiHero = `<div class="ai-hero"><span class="ai-orb">${icon('spark', 32)}</span><div><span class="eyebrow">PHÂN TÍCH TỪ BACKEND</span><h2>${displayName}, dữ liệu gợi ý nghề nghiệp<br><em>được lấy trực tiếp từ API</em></h2><p>${run ? `Run ${run.runId} · Độ tin cậy hồ sơ ${Math.round((run.profileConfidence || 0) * 100)}%` : 'Chưa có lần tạo gợi ý nào cho tài khoản này.'}</p></div><button class="btn btn-white" data-generate-recommendations>${icon('spark', 16)} Tạo gợi ý mới</button></div>`;
  const explanation = best ? `<div class="explain-grid">${(best.reasons || []).slice(0, 3).map((reason, index) => `<div><b>${String(index + 1).padStart(2, '0')}</b><h3>Lý do từ backend</h3><p>${escapeHtml(reason)}</p></div>`).join('') || '<div><b>API</b><h3>Chưa có lý do</h3><p>Backend chưa trả về giải thích cho gợi ý này.</p></div>'}</div>` : '<div class="empty-state"><h3>Chưa có gợi ý</h3><p>Bấm tạo gợi ý mới để backend phân tích hồ sơ.</p></div>';
  const score = best?.scores || {};
  const radar = `<div class="match-radar"><div class="radar-shape"></div><span class="r1">Sở thích <b>${Math.round(score.interest || 0)}</b></span><span class="r2">Năng lực <b>${Math.round(score.ability || 0)}</b></span><span class="r3">Kỹ năng <b>${Math.round(score.skill || 0)}</b></span><span class="r4">Thị trường <b>${Math.round(score.market || 0)}</b></span><span class="r5">Khả thi <b>${Math.round(score.feasibility || 0)}</b></span></div>`;
  const main = `<div class="recommend-layout"><div>${card('Lựa chọn phù hợp nhất', recommendationCard(best), { icon: 'star', className: 'primary-recommend' })}${card('Backend giải thích như thế nào?', explanation, { icon: 'bot' })}</div><div>${card('Điểm tương thích', radar, { icon: 'chart' })}${card('Thao tác', '<button class="btn btn-primary btn-block" data-generate-recommendations>Tạo lại từ backend ' + icon('arrow', 15) + '</button>', { icon: 'bot' })}</div></div>`;
  const alternatives = `<div class="alternative-grid">${recommendations.slice(1).map(item => `<article><span class="alt-icon">${icon('briefcase', 20)}</span><div><small>LỰA CHỌN #${item.rank}</small><h3>${escapeHtml(item.careerName)}</h3><p>${escapeHtml((item.reasons || [])[0] || item.group || 'Gợi ý từ backend')}</p></div><strong>${Math.round(item.scores?.finalScore || item.confidence || 0)}%</strong></article>`).join('') || '<div class="empty-state"><h3>Chưa có lựa chọn khác</h3><p>Backend chưa trả về thêm gợi ý.</p></div>'}</div>`;
  return `${aiHero}${main}<h2 class="section-title">Các lựa chọn phù hợp khác</h2>${alternatives}`;
}

export function bindRecommendationsEvents() {
  document.querySelectorAll('[data-generate-recommendations]').forEach(button => button.addEventListener('click', async () => {
    button.disabled = true;
    button.textContent = 'Đang tạo...';
    try {
      await generateRecommendations({});
      location.reload();
    } catch (error) {
      button.disabled = false;
      button.textContent = error.message || 'Thử lại';
    }
  }));
}
