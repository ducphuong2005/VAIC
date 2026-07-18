import { icon } from './components/icons.js';
import { isAuthenticated } from './core/authStore.js';
import { login, register } from './api/authApi.js';

const mode = document.body.dataset.auth || 'login';
const root = document.querySelector('#auth-root');

if (isAuthenticated()) location.replace('index.html');

function field(label, name, type = 'text', placeholder = '', extra = '') {
  return `<label>${label}<input name="${name}" type="${type}" placeholder="${placeholder}" ${extra}></label>`;
}

function authShell(content) {
  root.innerHTML = `<section class="auth-page">
    <div class="auth-brand"><span class="brand-mark">${icon('discover', 28)}</span><span>CAREER<br>COMPASS</span></div>
    <div class="auth-panel">
      <div class="auth-copy">
        <span class="eyebrow">ĐỊNH HƯỚNG NGHỀ NGHIỆP CÁ NHÂN HÓA</span>
        <h1>${mode === 'login' ? 'Chào mừng bạn quay lại' : 'Tạo tài khoản của bạn'}</h1>
        <p>${mode === 'login' ? 'Đăng nhập để tải hồ sơ, mini-game và gợi ý nghề nghiệp từ backend.' : 'Tạo tài khoản backend để Career Compass lưu tiến độ và cá nhân hóa trải nghiệm.'}</p>
      </div>
      ${content}
    </div>
  </section>`;
}

function renderLogin() {
  authShell(`<form class="auth-card" data-auth-form>
    <h2>Đăng nhập</h2>
    ${field('Email', 'email', 'email', 'ban@example.com', 'required')}
    ${field('Mật khẩu', 'password', 'password', 'Nhập mật khẩu', 'required minlength="8"')}
    <button class="btn btn-primary btn-block" type="submit">Đăng nhập ${icon('arrow', 16)}</button>
    <p class="form-message" data-auth-message></p>
    <p class="auth-switch">Chưa có tài khoản? <a href="register.html">Đăng ký ngay</a></p>
  </form>`);
}

function renderRegister() {
  authShell(`<form class="auth-card" data-auth-form>
    <h2>Đăng ký</h2>
    ${field('Họ và tên', 'fullName', 'text', 'Nguyễn Văn A', 'required')}
    ${field('Email', 'email', 'email', 'ban@example.com', 'required')}
    ${field('Số điện thoại', 'phone', 'tel', '0912 345 678')}
    ${field('Mật khẩu', 'password', 'password', 'Tối thiểu 8 ký tự', 'required minlength="8"')}
    <button class="btn btn-primary btn-block" type="submit">Tạo tài khoản ${icon('arrow', 16)}</button>
    <p class="form-message" data-auth-message></p>
    <p class="auth-switch">Đã có tài khoản? <a href="login.html">Đăng nhập</a></p>
  </form>`);
}

function bindForm() {
  const form = document.querySelector('[data-auth-form]');
  form?.addEventListener('submit', async event => {
    event.preventDefault();
    const button = form.querySelector('button[type=submit]');
    const message = form.querySelector('[data-auth-message]');
    const data = Object.fromEntries(new FormData(form));
    button.disabled = true;
    message.textContent = '';
    try {
      await (mode === 'register' ? register(data) : login(data));
      const redirect = sessionStorage.getItem('careerCompass.redirectAfterLogin');
      sessionStorage.removeItem('careerCompass.redirectAfterLogin');
      location.href = redirect || 'index.html';
    } catch (error) {
      message.textContent = error.message || 'Không thể kết nối backend hoặc xử lý tài khoản.';
      button.disabled = false;
    }
  });
}

mode === 'register' ? renderRegister() : renderLogin();
bindForm();
