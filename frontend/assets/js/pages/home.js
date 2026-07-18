import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { recommendationCard, skillStrip } from '../components/widgets.js';
import { trendingCareers } from '../data/trendingCareers.js';
import { login, register } from '../api/authApi.js';
import { createChatSession, getChatSession, getChatSessions, sendChatMessage } from '../api/chatApi.js';
import { getSkillsInDemand } from '../api/marketApi.js';
import { getLatestRecommendations } from '../api/recommendationApi.js';
import { friendlyErrorMessage } from '../core/errors.js';
import {
  clearTokens,
  getAccessToken,
  getDisplayName,
  isAuthenticated,
  setCurrentUser,
  setTokens
} from '../core/authStore.js';

async function optional(promise, fallback = null) {
  try {
    return await promise;
  } catch {
    return fallback;
  }
}

export async function homePage() {
  const displayName = getDisplayName();
  const [skillsRaw, latest] = await Promise.all([
    optional(getSkillsInDemand(5), []),
    isAuthenticated() ? optional(getLatestRecommendations()) : null
  ]);
  const recommendation = latest?.recommendations?.[0] || null;
  const game = `<div class="game-feature"><div><span class="pill">Backend mini-game</span><h3>Mini-game nghề nghiệp</h3><p>Danh sách trò chơi và phiên chơi được lấy trực tiếp từ API.</p></div></div><a class="btn btn-primary game-btn" href="minigame.html">Chơi ngay</a>`;
  const maxTrend = Math.max(...trendingCareers.map(item => item.value), 1);
  const trends = `<div class="trend-static-head"><span>Nhóm ngành</span><span>Xu hướng</span><span>Dữ liệu</span></div><div class="trend-static-list">${trendingCareers.map(item => `<div class="trend-static-row"><div class="trend-name"><strong>${item.name}</strong><small>${item.source}</small></div><div class="trend-bar"><i style="width:${(item.value / maxTrend) * 100}%"></i><b>${item.trend}</b></div><div class="trend-value">${item.value.toLocaleString('vi-VN')}</div></div>`).join('')}</div><a class="btn btn-ghost btn-center" href="careers.html">Xem tất cả nhóm ngành ${icon('arrow', 15)}</a>`;
  const market = `<div class="market-grid">${(skillsRaw || []).slice(0, 3).map(skill => `<div><span>${skill.skillName}</span><strong>${skill.postingCount}</strong><small>${skill.source || 'backend'}</small></div>`).join('') || '<div><span>Chưa có dữ liệu</span><strong>0</strong><small>backend</small></div>'}</div>`;
  const chat = `<div class="chat-box"><div class="message bot">Xin chào ${displayName}!<br>Mình là Career Compass AI. Bạn muốn khám phá hướng nghề nào hôm nay?<time>API</time></div></div><form class="chat-input" data-chat-form><input placeholder="Nhập tin nhắn của bạn..." aria-label="Tin nhắn"><button>${icon('send', 18)}</button></form>`;
  const recommendContent = isAuthenticated() ? recommendationCard(recommendation, true) : '<div class="empty-state"><h3>Cần đăng nhập</h3><p>Đăng nhập để tải gợi ý cá nhân hóa từ backend.</p><a class="btn btn-primary" href="login.html">Đăng nhập</a></div>';
  return `<div class="dashboard-grid"><div class="dash-left">${card('Mini-game nổi bật', game, { icon: 'game', action: '<a href="minigame.html" class="text-link">Xem tất cả ›</a>', className: 'game-card' })}</div><div class="dash-middle">${card('Nhóm ngành đang trending', trends, { icon: 'chart', className: 'trend-card trend-static-card' })}${card('Kỹ năng thị trường cần', market, { icon: 'info', className: 'market-card' })}</div><div class="dash-right">${card('Career Compass AI', chat, { icon: 'bot', className: 'chat-card' })}${card('Gợi ý dành riêng cho bạn', recommendContent, { icon: 'star', className: 'recommend-wrap' })}</div></div>${card('Kỹ năng thị trường nổi bật', skillStrip(skillsRaw || []), { className: 'strength-card' })}`;
}

const chatSessionKey = 'careerCompass.chatSessionId';
const demoUser = {
  email: 'demo@career-compass.local',
  password: 'DemoPass123',
  fullName: 'Career Compass Demo'
};

function chatTime() {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function appendMessage(box, sender, content, suffix = '', timeLabel = chatTime()) {
  const el = document.createElement('div');
  el.className = `message ${sender}`;
  el.textContent = content;
  const time = document.createElement('time');
  time.textContent = `${timeLabel}${suffix}`;
  el.append(time);
  box.append(el);
  box.scrollTop = box.scrollHeight;
  return el;
}

function storedChatTime(value) {
  if (!value) return 'DB';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return 'DB';
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function appendStoredMessage(box, message = {}) {
  const sender = message.sender === 'USER' ? 'me' : 'bot';
  appendMessage(box, sender, message.content || '', sender === 'me' ? ' ✓' : '', storedChatTime(message.createdAt));
}

async function ensureChatSession() {
  await ensureDemoAuth();
  const stored = sessionStorage.getItem(chatSessionKey);
  if (stored) return stored;
  const sessions = await getChatSessions();
  const existing = sessions?.[0]?.id;
  if (existing) {
    sessionStorage.setItem(chatSessionKey, existing);
    return existing;
  }
  const created = await createChatSession('Career Compass AI');
  sessionStorage.setItem(chatSessionKey, created.id);
  return created.id;
}

async function hydrateChatHistory(box) {
  if (!getAccessToken()) return;
  try {
    const sessionId = await ensureChatSession();
    const detail = await getChatSession(sessionId);
    const messages = detail?.messages || [];
    if (!messages.length) return;
    box.innerHTML = '';
    messages.forEach(message => appendStoredMessage(box, message));
  } catch (error) {
    if (error?.status === 401 || error?.status === 403 || error?.status === 404) {
      sessionStorage.removeItem(chatSessionKey);
    }
  }
}

async function ensureDemoAuth() {
  if (getAccessToken()) return;
  let auth;
  try {
    auth = await register(demoUser);
  } catch (error) {
    if (error?.status !== 409) throw error;
    auth = await login({ email: demoUser.email, password: demoUser.password });
  }
  setTokens(auth);
  setCurrentUser(auth.user);
}

async function sendWithFreshSession(content) {
  try {
    return await sendChatMessage(await ensureChatSession(), content);
  } catch (error) {
    if (error?.status === 401 || error?.status === 403) {
      clearTokens();
      sessionStorage.removeItem(chatSessionKey);
      await ensureDemoAuth();
      return sendChatMessage(await ensureChatSession(), content);
    }
    if (error?.status !== 404) throw error;
    sessionStorage.removeItem(chatSessionKey);
    return sendChatMessage(await ensureChatSession(), content);
  }
}

export function bindHomeEvents() {
  const form = document.querySelector('[data-chat-form]');
  const box = document.querySelector('.chat-box');
  if (!form || !box) return;
  hydrateChatHistory(box);
  const input = form.querySelector('input');
  const button = form.querySelector('button');
  form.addEventListener('submit', async event => {
    event.preventDefault();
    const content = input.value.trim();
    if (!content) return;
    appendMessage(box, 'me', content, ' ✓');
    input.value = '';
    input.disabled = true;
    button.disabled = true;
    const pending = appendMessage(box, 'bot', 'Đang phân tích...');
    try {
      const reply = await sendWithFreshSession(content);
      pending.firstChild.textContent = reply.content || 'Mình chưa tạo được phản hồi phù hợp. Bạn thử hỏi lại giúp mình nhé.';
    } catch (error) {
      pending.firstChild.textContent = friendlyErrorMessage(error);
    } finally {
      input.disabled = false;
      button.disabled = false;
      input.focus();
    }
  });
}
