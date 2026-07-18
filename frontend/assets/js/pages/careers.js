import { careerGrid } from '../components/widgets.js';
import { icon } from '../components/icons.js';
import { getCareerClusters, getCareers, searchCareers } from '../api/careerApi.js';
import { saveFavoriteCareer, saveFavoriteLink } from '../api/favoriteApi.js';

let careers = [];

export async function careersPage() {
  const [careerPage, clusters] = await Promise.all([
    getCareers({ size: 24 }),
    getCareerClusters()
  ]);
  careers = careerPage?.content || [];
  const hero = `<div class="career-search-hero"><span class="eyebrow">DỮ LIỆU TỪ BACKEND</span><h2>Tìm công việc khiến bạn <em>muốn thức dậy mỗi sáng</em></h2><form class="big-search" data-career-search-form>${icon('search')}<input name="q" data-career-search placeholder="Tìm theo tên nghề nghiệp, kỹ năng..."><button class="btn btn-primary">Tìm kiếm</button></form></div>`;
  const chips = `<div class="category-chips" data-career-chips><button class="active" data-cluster="">Tất cả</button>${(clusters || []).slice(0, 8).map(cluster => `<button data-cluster="${cluster}">${cluster}</button>`).join('')}</div>`;
  const toolbar = `<div class="browse-head"><div><h2>Nghề nghiệp từ backend</h2><p>${careerPage?.totalElements ?? careers.length} nghề nghiệp đang có trong hệ thống.</p></div><div class="filter-controls"><select data-career-category><option value="">Tất cả nhóm ngành</option>${(clusters || []).map(cluster => `<option value="${cluster}">${cluster}</option>`).join('')}</select></div></div>`;
  return `${hero}${chips}${toolbar}<div data-career-results>${careerGrid(careers)}</div>`;
}

export function bindCareersEvents() {
  const results = document.querySelector('[data-career-results]');
  const render = items => { results.innerHTML = careerGrid(items); };
  document.querySelector('[data-career-search-form]')?.addEventListener('submit', async event => {
    event.preventDefault();
    const q = new FormData(event.currentTarget).get('q');
    render((await searchCareers(q, { size: 24 }))?.content || []);
  });
  document.querySelector('[data-career-category]')?.addEventListener('change', async event => {
    render((await getCareers({ cluster: event.target.value, size: 24 }))?.content || []);
  });
  document.querySelectorAll('[data-cluster]').forEach(button => button.addEventListener('click', async () => {
    document.querySelectorAll('[data-cluster]').forEach(x => x.classList.remove('active'));
    button.classList.add('active');
    render((await getCareers({ cluster: button.dataset.cluster, size: 24 }))?.content || []);
  }));
  results?.addEventListener('click', async event => {
    const linkButton = event.target.closest('[data-save-link]');
    if (linkButton) {
      linkButton.disabled = true;
      try {
        await saveFavoriteLink({
          linkType: linkButton.dataset.linkType || 'JOB',
          provider: linkButton.dataset.provider || 'Job board',
          title: linkButton.dataset.title || 'Job link',
          url: linkButton.dataset.url,
          description: linkButton.dataset.description || ''
        });
        linkButton.textContent = 'Đã lưu';
        linkButton.classList.add('saved');
      } catch (error) {
        linkButton.title = error.message || 'Không thể lưu link.';
        linkButton.disabled = false;
      }
      return;
    }
    const button = event.target.closest('[data-save]');
    if (!button || !button.dataset.save) return;
    button.disabled = true;
    try {
      await saveFavoriteCareer(button.dataset.save);
      button.classList.add('saved');
    } catch (error) {
      button.title = error.message || 'Không thể lưu nghề.';
      button.disabled = false;
    }
  });
}
