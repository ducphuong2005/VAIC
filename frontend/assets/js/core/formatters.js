export const escapeHtml=value=>String(value??'').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
export const formatPercent=value=>`${new Intl.NumberFormat('vi-VN',{maximumFractionDigits:1}).format(Number(value)||0)}%`;
export const formatCurrencyVnd=value=>value==null?'Chưa cập nhật':`${new Intl.NumberFormat('vi-VN',{maximumFractionDigits:1}).format(Number(value)/1e6)} triệu`;
export const formatDateTime=value=>value?new Intl.DateTimeFormat('vi-VN',{dateStyle:'short',timeStyle:'short'}).format(new Date(value)):'Chưa cập nhật';
