import{friendlyErrorMessage}from'../core/errors.js';import{escapeHtml}from'../core/formatters.js';
export const renderApiError=error=>`<div class="empty-state api-error" role="alert"><h3>Chưa thể tải dữ liệu</h3><p>${escapeHtml(friendlyErrorMessage(error))}</p><button class="btn btn-primary" data-page-retry>Thử lại</button></div>`;
