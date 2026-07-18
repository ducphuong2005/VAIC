import { friendlyErrorMessage } from '../core/errors.js';
import { escapeHtml } from '../core/formatters.js';

const isLoginRequired = error => /đăng nhập/i.test(error?.message || '') || error?.status === 401;

export const renderApiError = error => {
  const loginRequired = isLoginRequired(error);
  const message = friendlyErrorMessage(error);
  const action = loginRequired
    ? '<a class="btn btn-primary" href="login.html">Đăng nhập</a>'
    : '<button class="btn btn-primary" data-page-retry>Thử lại</button>';
  return `<div class="empty-state api-error" role="alert"><h3>${loginRequired ? 'Vui lòng đăng nhập' : 'Chưa thể tải dữ liệu'}</h3><p>${escapeHtml(message)}</p>${action}</div>`;
};
