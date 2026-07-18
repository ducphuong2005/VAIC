import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { getActivityHistory } from '../api/historyApi.js';
import { isAuthenticated } from '../core/authStore.js';
import { escapeHtml, formatDateTime } from '../core/formatters.js';

const iconForType = type => ({
  MINIGAME: 'game',
  FAVORITE: 'bookmark',
  PROFILE: 'user',
  ASSESSMENT: 'discover',
  CHAT: 'bot',
  RECOMMENDATION: 'spark'
}[String(type || '').toUpperCase()] || 'clock');

export async function historyPage() {
  if (!isAuthenticated()) throw new Error('Bạn cần đăng nhập để tải lịch sử hoạt động từ backend.');
  const activities = await getActivityHistory();
  const filters = `<div class="history-filter"><div class="tabs"><button class="active">Tất cả</button></div><select><option>Dữ liệu backend</option></select></div>`;
  const timeline = `<div class="activity-timeline">${(activities || []).map((item, i) => `<article><div class="activity-icon ai-${i % 4}">${icon(iconForType(item.activityType), 19)}</div><div><span class="activity-date">${formatDateTime(item.createdAt)}</span><h3>${escapeHtml(item.activityType || 'Hoạt động')}</h3><p>${escapeHtml(item.message || item.metadata || 'Backend chưa có nội dung.')}</p></div><button class="icon-btn">${icon('chevron', 17)}</button></article>`).join('') || '<div class="empty-state"><h3>Chưa có lịch sử</h3><p>Backend chưa ghi nhận hoạt động nào.</p></div>'}</div>`;
  return `${filters}<div class="history-layout"><div>${card('Hoạt động gần đây', timeline, { icon: 'clock' })}</div><aside>${card('Thống kê', `<div class="history-stats"><div><strong>${activities?.length || 0}</strong><span>Hoạt động từ backend</span></div></div>`, { icon: 'chart' })}${card('Dữ liệu của bạn', '<p class="muted">Lịch sử hoạt động đang được đọc từ API backend.</p>', {})}</aside></div>`;
}
