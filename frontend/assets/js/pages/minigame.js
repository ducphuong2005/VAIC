import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { getMiniGames, startMiniGame } from '../api/minigameApi.js';
import { isAuthenticated } from '../core/authStore.js';
import { escapeHtml } from '../core/formatters.js';
import { toMiniGameViewModel } from '../mappers/minigameMapper.js';

export async function minigamePage() {
  const games = (await getMiniGames() || []).map(toMiniGameViewModel);
  const filters = `<div class="filter-row"><div class="tabs" data-game-filter><button class="active" data-filter="all">Tất cả</button><button data-filter="analysis">Phân tích</button><button data-filter="creative">Sáng tạo</button></div></div>`;
  const grid = games.length
    ? `<div class="games-grid">${games.map((game, i) => `<article class="game-tile" data-game-category="${game.type.toLowerCase().includes('creative') ? 'creative' : 'analysis'}"><div class="game-visual gv-${i % 6}"><span>${icon('game', 30)}</span></div><div class="game-content"><span class="pill">${escapeHtml(game.type)}</span><h3>${escapeHtml(game.title)}</h3><p>${escapeHtml(game.description)}</p><div class="game-meta"><span>${game.minutes} phút</span></div><button class="btn btn-primary btn-block" data-game-start="${escapeHtml(game.id)}">Chơi ngay ${icon('play', 15)}</button></div></article>`).join('')}</div>`
    : '<div class="empty-state"><h3>Chưa có mini-game</h3><p>Danh sách sẽ xuất hiện khi backend có dữ liệu.</p></div>';
  return `${filters}${grid}${card('Kết quả năng lực', '<p class="muted">Bạn có thể xem danh sách mini-game mà chưa đăng nhập. Khi bấm chơi, hệ thống sẽ yêu cầu đăng nhập để backend tạo phiên và lưu kết quả.</p>', { icon: 'trophy' })}`;
}

export function bindMinigameEvents() {
  document.querySelectorAll('[data-game-filter] button').forEach(button => button.addEventListener('click', () => {
    document.querySelectorAll('[data-game-filter] button').forEach(x => x.classList.remove('active'));
    button.classList.add('active');
    document.querySelectorAll('.game-tile').forEach(tile => {
      tile.hidden = button.dataset.filter !== 'all' && tile.dataset.gameCategory !== button.dataset.filter;
    });
  }));
  document.querySelectorAll('[data-game-start]').forEach(button => button.addEventListener('click', async () => {
    if (!isAuthenticated()) {
      sessionStorage.setItem('careerCompass.redirectAfterLogin', location.href);
      location.href = 'login.html';
      return;
    }
    button.disabled = true;
    button.textContent = 'Đang bắt đầu...';
    try {
      const session = await startMiniGame(button.dataset.gameStart);
      sessionStorage.setItem('careerCompass.minigameSession', JSON.stringify(session));
      button.textContent = 'Đã tạo phiên chơi';
    } catch (error) {
      button.disabled = false;
      button.textContent = error.message || 'Thử lại';
    }
  }));
}
