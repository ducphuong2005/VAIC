import { careerGrid } from '../components/widgets.js';
import { deleteFavoriteLink, getFavoriteCareers, getFavoriteCourses, getFavoriteLinks } from '../api/favoriteApi.js';
import { icon } from '../components/icons.js';
import { isAuthenticated } from '../core/authStore.js';
import { escapeHtml } from '../core/formatters.js';

export async function favoritesPage() {
  if (!isAuthenticated()) throw new Error('Bạn cần đăng nhập để tải danh sách yêu thích từ backend.');
  const [careers, courses, links] = await Promise.all([getFavoriteCareers(), getFavoriteCourses(), getFavoriteLinks()]);
  const jobLinks = (links || []).filter(link => link.linkType === 'JOB');
  const learningLinks = (links || []).filter(link => link.linkType === 'LEARNING');
  const courseList = (courses || []).map(course => `<article class="career-card"><div class="career-body"><div class="match-line"><span>${course.durationHours || 0} giờ</span><strong>Khóa học</strong></div><h3>${escapeHtml(course.title)}</h3><p>${escapeHtml(course.description || 'Backend chưa có mô tả.')}</p><a class="btn btn-soft btn-block" href="${escapeHtml(course.url || '#')}" target="_blank" rel="noreferrer">Mở khóa học ${icon('arrow', 15)}</a></div></article>`).join('');
  const linkCards = items => items.map(link => `<article class="career-card favorite-link-card"><div class="career-body"><div class="match-line"><span>${escapeHtml(link.provider || 'Link')}</span><strong>${escapeHtml(link.linkType || 'LINK')}</strong></div><h3>${escapeHtml(link.title || 'Liên kết đã lưu')}</h3><p>${escapeHtml(link.description || link.url || '')}</p><div class="favorite-link-actions"><a class="btn btn-soft" href="${escapeHtml(link.url || '#')}" target="_blank" rel="noreferrer">Mở link ${icon('arrow', 15)}</a><button class="btn btn-outline" type="button" data-delete-link="${link.id}">Bỏ lưu</button></div></div></article>`).join('');
  const linkList = items => `<div class="career-grid">${linkCards(items) || '<div class="empty-state"><h3>Chưa có link yêu thích</h3><p>Bấm Lưu ở job hoặc bài học để thêm vào đây.</p></div>'}</div>`;
  const lessonCards = `${courseList}${linkCards(learningLinks)}`;
  return `<div class="favorite-tabs tabs"><button class="active" data-fav-tab="career">Nghề nghiệp <span>${careers?.length || 0}</span></button><button data-fav-tab="job">Job link <span>${jobLinks.length}</span></button><button data-fav-tab="lesson">Bài học <span>${learningLinks.length + (courses?.length || 0)}</span></button></div><div class="saved-toolbar"><div><h2>Nghề nghiệp đã lưu</h2><p>Danh sách này được tải từ backend.</p></div><a class="btn btn-outline" href="careers.html">${icon('briefcase', 16)} Khám phá thêm</a></div><div id="favorite-content">${careerGrid(careers || [])}</div><template data-course-list><div class="career-grid">${lessonCards || '<div class="empty-state"><h3>Chưa có bài học yêu thích</h3><p>Lưu link bài học trong lộ trình học tập.</p></div>'}</div></template><template data-job-list>${linkList(jobLinks)}</template><template data-career-list>${careerGrid(careers || [])}</template>`;
}

export function bindFavoritesEvents() {
  const content = document.querySelector('#favorite-content');
  document.querySelectorAll('[data-fav-tab]').forEach(button => button.addEventListener('click', () => {
    document.querySelectorAll('[data-fav-tab]').forEach(x => x.classList.remove('active'));
    button.classList.add('active');
    const selector = button.dataset.favTab === 'job'
      ? '[data-job-list]'
      : button.dataset.favTab === 'lesson'
        ? '[data-course-list]'
        : '[data-career-list]';
    content.innerHTML = document.querySelector(selector).innerHTML;
  }));
  document.body.addEventListener('click', async event => {
    const button = event.target.closest('[data-delete-link]');
    if (!button) return;
    button.disabled = true;
    try {
      await deleteFavoriteLink(button.dataset.deleteLink);
      button.closest('.career-card')?.remove();
    } catch (error) {
      button.title = error.message || 'Không thể bỏ lưu.';
      button.disabled = false;
    }
  });
}
