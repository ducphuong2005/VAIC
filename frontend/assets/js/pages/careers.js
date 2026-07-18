import { careerGrid } from '../components/widgets.js';
import { icon } from '../components/icons.js';
import { getCareerClusters, getCareers, searchCareers } from '../api/careerApi.js';
import { saveFavoriteCareer } from '../api/favoriteApi.js';
import { getLatestRecommendations } from '../api/recommendationApi.js';
import { isAuthenticated } from '../core/authStore.js';

let careers = [];

async function optional(promise, fallback = null) {
  try {
    return await promise;
  } catch {
    return fallback;
  }
}

export async function careersPage() {
  const [careerPage, clusters, latest] = await Promise.all([
    getCareers({ size: 24 }),
    getCareerClusters(),
    isAuthenticated() ? optional(getLatestRecommendations()) : null
  ]);
  careers = careerPage?.content || [];
  const hero = `<div class="career-search-hero"><span class="eyebrow">DỮ LIỆU TỪ BACKEND</span><h2>Tìm công việc khiến bạn <em>muốn thức dậy mỗi sáng</em></h2><form class="big-search" data-career-search-form>${icon('search')}<input name="q" data-career-search placeholder="Tìm theo tên nghề nghiệp, kỹ năng..."><button class="btn btn-primary">Tìm kiếm</button></form></div>`;
  const aiDiscovery = latest?.recommendations?.length
    ? `<section class="ai-discovery-panel"><div class="browse-head"><div><h2>Khám phá nghề từ mini-game</h2><p>LLM đã dùng kết quả của bạn và dữ liệu jobs.csv để chọn các hướng nên xem trước.</p></div><a class="btn btn-outline" href="recommendations.html">Xem phân tích AI ${icon('arrow', 15)}</a></div>${careerGrid(latest.recommendations.slice(0, 3))}</section>`
    : '';
  const chips = `<div class="category-chips" data-career-chips><button class="active" data-cluster="">Tất cả</button>${(clusters || []).slice(0, 8).map(cluster => `<button data-cluster="${cluster}">${cluster}</button>`).join('')}</div>`;
  const toolbar = `<div class="browse-head"><div><h2>Nghề nghiệp từ backend</h2><p>${careerPage?.totalElements ?? careers.length} nghề nghiệp đang có trong hệ thống.</p></div><div class="filter-controls"><select data-career-category><option value="">Tất cả nhóm ngành</option>${(clusters || []).map(cluster => `<option value="${cluster}">${cluster}</option>`).join('')}</select></div></div>`;
  return `${hero}${aiDiscovery}${chips}${toolbar}<div data-career-results>${careerGrid(careers)}</div>`;
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
    const button = event.target.closest('[data-save]');
    if (!button || !button.dataset.save) return;
    button.disabled = true;
    try {
      await saveFavoriteCareer(button.dataset.save);
      button.classList.add('active');
    } catch (error) {
      button.title = error.message || 'Không thể lưu nghề.';
      button.disabled = false;
    }
  });
}
