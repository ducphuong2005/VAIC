import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { careerRows, recommendationCard, skillStrip, toTrendingCareer } from '../components/widgets.js';
import { getDisplayName, isAuthenticated } from '../core/authStore.js';
import { getTrendingCareers, getSkillsInDemand } from '../api/marketApi.js';
import { getLatestRecommendations } from '../api/recommendationApi.js';

async function optional(promise, fallback = null) {
  try { return await promise; } catch { return fallback; }
}

export async function homePage() {
  const displayName = getDisplayName();
  const [trendingRaw, skillsRaw, latest] = await Promise.all([
    getTrendingCareers(5),
    getSkillsInDemand(5),
    isAuthenticated() ? optional(getLatestRecommendations()) : null
  ]);
  const trending = (trendingRaw || []).map(toTrendingCareer);
  const recommendation = latest?.recommendations?.[0] || null;
  const survey = `<p class="muted">Dữ liệu hồ sơ được lưu và tính toán bởi backend.</p><div class="step-line"><span>Hồ sơ backend</span><b><a href="profile.html">Cập nhật</a></b></div><a class="btn btn-primary btn-block" href="profile.html">Mở hồ sơ ${icon('arrow', 16)}</a>`;
  const game = `<div class="game-feature"><div><span class="pill">Backend mini-game</span><h3>Mini-game nghề nghiệp</h3><p>Danh sách trò chơi và phiên chơi được lấy trực tiếp từ API.</p></div></div><a class="btn btn-primary game-btn" href="minigame.html">Chơi ngay</a>`;
  const trends = `<div class="table-head"><span>Nhóm ngành</span><span>Xu hướng</span><span>Dữ liệu</span></div>${careerRows(trending)}<a class="btn btn-ghost btn-center" href="careers.html">Xem tất cả nhóm ngành ${icon('arrow', 15)}</a>`;
  const market = `<div class="market-grid">${(skillsRaw || []).slice(0, 3).map(skill => `<div><span>${skill.skillName}</span><strong>${skill.postingCount}</strong><small>${skill.source || 'backend'}</small></div>`).join('') || '<div><span>Chưa có dữ liệu</span><strong>0</strong><small>backend</small></div>'}</div>`;
  const chat = `<div class="chat-box"><div class="message bot">Xin chào ${displayName}!<br>Dữ liệu hiển thị trên dashboard đang được tải từ backend.<time>API</time></div></div><form class="chat-input" data-chat-form><input placeholder="Tính năng chat dùng backend khi bạn bắt đầu phiên..." aria-label="Tin nhắn"><button>${icon('send', 18)}</button></form>`;
  const recommendContent = isAuthenticated() ? recommendationCard(recommendation, true) : '<div class="empty-state"><h3>Cần đăng nhập</h3><p>Đăng nhập để tải gợi ý cá nhân hóa từ backend.</p><a class="btn btn-primary" href="login.html">Đăng nhập</a></div>';
  return `<div class="dashboard-grid"><div class="dash-left">${card('Khám phá bản thân', survey, { icon: 'user', className: 'survey-card' })}${card('Mini-game nổi bật', game, { icon: 'game', action: '<a href="minigame.html" class="text-link">Xem tất cả ›</a>', className: 'game-card' })}</div><div class="dash-middle">${card('Nhóm ngành đang trending', trends, { icon: 'chart', className: 'trend-card' })}${card('Kỹ năng thị trường cần', market, { icon: 'info', className: 'market-card' })}</div><div class="dash-right">${card('Career Compass AI', chat, { icon: 'bot', className: 'chat-card' })}${card('Gợi ý dành riêng cho bạn', recommendContent, { icon: 'star', className: 'recommend-wrap' })}</div></div>${card('Kỹ năng thị trường nổi bật', skillStrip(skillsRaw || []), { className: 'strength-card' })}`;
}
