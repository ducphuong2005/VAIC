import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { clearTokens, getCurrentUser, getUserInitials, isAuthenticated } from '../core/authStore.js';

function toggle(label, desc, checked = true) {
  return `<label class="setting-row"><span><strong>${label}</strong><small>${desc}</small></span><input class="toggle" type="checkbox" ${checked ? 'checked' : ''} disabled></label>`;
}

export function settingsPage() {
  if (!isAuthenticated()) throw new Error('Bạn cần đăng nhập để tải cài đặt tài khoản từ backend.');
  const user = getCurrentUser() || {};
  const initials = getUserInitials(user);
  const menu = `<div class="settings-menu"><button class="active" data-setting="account">${icon('user')} Tài khoản</button><button data-setting="notification">${icon('bell')} Thông báo</button><button data-setting="privacy">${icon('lock')} Quyền riêng tư</button></div>`;
  const account = `<div id="settings-panel"><section class="settings-section"><h3>Thông tin tài khoản backend</h3><div class="avatar-setting"><span class="big-avatar">${initials}</span><div><p>Dữ liệu tài khoản đang hiển thị từ response đăng nhập/đăng ký backend.</p></div></div><div class="form-grid"><label>Họ và tên<input value="${user.fullName || ''}" readonly></label><label>Email<input type="email" value="${user.email || ''}" readonly></label><label>Số điện thoại<input value="${user.phone || ''}" readonly></label></div></section><section class="settings-section"><h3>Tùy chọn trải nghiệm</h3>${toggle('Hiển thị trạng thái học tập', 'Chưa có endpoint backend để cập nhật tùy chọn này.', false)}</section><section class="settings-section danger-zone"><h3>Phiên đăng nhập</h3><p>Đăng xuất khỏi tài khoản hiện tại trên thiết bị này.</p><button class="btn btn-danger" type="button" data-logout-settings>${icon('lock', 16)} Đăng xuất</button></section></div>`;
  return `<div class="settings-layout">${menu}${card('Cài đặt tài khoản', account, { className: 'settings-card' })}</div>`;
}

export function bindSettingsEvents() {
  document.querySelector('[data-logout-settings]')?.addEventListener('click', () => {
    clearTokens();
    location.href = 'index.html';
  });
}
