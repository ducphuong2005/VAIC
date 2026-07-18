import { careerGrid } from '../components/widgets.js';
import { getFavoriteCareers, getFavoriteCourses } from '../api/favoriteApi.js';
import { icon } from '../components/icons.js';
import { isAuthenticated } from '../core/authStore.js';
import { escapeHtml } from '../core/formatters.js';

export async function favoritesPage() {
  if (!isAuthenticated()) throw new Error('Bạn cần đăng nhập để tải danh sách yêu thích từ backend.');
  const [careers, courses] = await Promise.all([getFavoriteCareers(), getFavoriteCourses()]);
  const courseList = (courses || []).map(course => `<article class="career-card"><div class="career-body"><div class="match-line"><span>${course.durationHours || 0} giờ</span><strong>Khóa học</strong></div><h3>${escapeHtml(course.title)}</h3><p>${escapeHtml(course.description || 'Backend chưa có mô tả.')}</p><a class="btn btn-soft btn-block" href="${escapeHtml(course.url || '#')}" target="_blank" rel="noreferrer">Mở khóa học ${icon('arrow', 15)}</a></div></article>`).join('');
  return `<div class="favorite-tabs tabs"><button class="active" data-fav-tab="career">Nghề nghiệp <span>${careers?.length || 0}</span></button><button data-fav-tab="course">Khóa học <span>${courses?.length || 0}</span></button></div><div class="saved-toolbar"><div><h2>Nghề nghiệp đã lưu</h2><p>Danh sách này được tải từ backend.</p></div><button class="btn btn-outline">${icon('chart', 16)} So sánh nghề</button></div><div id="favorite-content">${careerGrid(careers || [])}</div><template data-course-list><div class="career-grid">${courseList || '<div class="empty-state"><h3>Chưa có khóa học yêu thích</h3><p>Backend chưa trả về khóa học đã lưu.</p></div>'}</div></template><template data-career-list>${careerGrid(careers || [])}</template>`;
}

export function bindFavoritesEvents() {
  const content = document.querySelector('#favorite-content');
  document.querySelectorAll('[data-fav-tab]').forEach(button => button.addEventListener('click', () => {
    document.querySelectorAll('[data-fav-tab]').forEach(x => x.classList.remove('active'));
    button.classList.add('active');
    content.innerHTML = document.querySelector(button.dataset.favTab === 'course' ? '[data-course-list]' : '[data-career-list]').innerHTML;
  }));
}
