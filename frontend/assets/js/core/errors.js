export class ApiError extends Error {
  constructor(message, { status = 0, code = 'API_ERROR', validationErrors = {}, cause } = {}) {
    super(message, { cause });
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.validationErrors = validationErrors;
  }
}

export function friendlyErrorMessage(error) {
  if (error?.code === 'TIMEOUT') return 'Yêu cầu quá thời gian. Vui lòng thử lại.';
  if (error?.code === 'NETWORK') return 'Không kết nối được backend. Kiểm tra backend và cổng API.';
  if (error?.message?.includes('LLM')) return error.message;
  if (error?.status === 401 || error?.status === 403) return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
  if (error?.status >= 500) return 'Hệ thống đang bận. Vui lòng thử lại sau.';
  return error?.message || 'Không thể tải dữ liệu.';
}
