import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { getAssessments, startAssessment } from '../api/assessmentApi.js';
import { escapeHtml } from '../core/formatters.js';

export async function discoveryPage() {
  const tests = await getAssessments();
  const intro = `<div class="discovery-hero"><div><span class="eyebrow">ASSESSMENT TỪ BACKEND</span><h2>Khám phá bản thân<br><em>bằng dữ liệu từ API</em></h2><p>Danh sách bài đánh giá bên dưới được lấy trực tiếp từ backend.</p></div><div class="hero-orbit"><span>${icon('discover', 44)}</span><i>✦</i><b>✧</b></div></div>`;
  const cards = `<div class="test-grid">${(tests || []).map((test, i) => `<article class="test-card"><div class="test-icon color-${i}">${icon(['discover', 'heart', 'spark', 'star'][i % 4], 25)}</div><div class="test-status">${escapeHtml(test.assessmentType || 'Assessment')}</div><h3>${escapeHtml(test.title)}</h3><p>${escapeHtml(test.description || 'Backend chưa có mô tả.')}</p><small>${test.estimatedMinutes || 0} phút</small><button class="btn ${i === 0 ? 'btn-primary' : 'btn-soft'} btn-block" data-test-start="${escapeHtml(test.id)}">Bắt đầu ${icon('arrow', 15)}</button></article>`).join('') || '<div class="empty-state"><h3>Chưa có bài đánh giá</h3><p>Backend chưa trả về assessment nào.</p></div>'}</div>`;
  const result = '<div class="result-preview"><div><span>Kết quả đánh giá</span><h3>Được backend tính sau khi hoàn thành</h3><p>Trang này không dùng chân dung mẫu. Kết quả thật sẽ đến từ API assessment session/result.</p></div></div>';
  return `${intro}${cards}${card('Kết quả từ backend', result, { icon: 'spark' })}`;
}

export function bindDiscoveryEvents() {
  document.querySelectorAll('[data-test-start]').forEach(button => button.addEventListener('click', async () => {
    button.disabled = true;
    button.textContent = 'Đang tạo phiên...';
    try {
      const session = await startAssessment(button.dataset.testStart);
      sessionStorage.setItem('careerCompass.assessmentSession', JSON.stringify(session));
      button.textContent = 'Đã tạo phiên';
    } catch (error) {
      button.disabled = false;
      button.textContent = error.message || 'Thử lại';
    }
  }));
}
