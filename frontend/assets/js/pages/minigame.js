import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';
import { getMiniGames, startMiniGame, recordMiniGameActions, completeMiniGame } from '../api/minigameApi.js';
import { isAuthenticated } from '../core/authStore.js';
import { escapeHtml } from '../core/formatters.js';
import { escapeRooms, riasecLabels } from '../data/escapeRoomData.js';

const letters = ['A', 'B', 'C'];
const totalPuzzles = escapeRooms.reduce((sum, room) => sum + room.steps.filter(step => step.kind === 'puzzle').length, 0);
const roomImageFolders = ['R1', 'R2', 'R3', 'R4', 'R5', 'R6'];
const roomImageFileOverrides = {
  'R2-4': 'R3M4.png',
  'R2-5': 'R3M5.png'
};
let state;

function initialState(backendGameId = '') {
  return {
    backendGameId,
    roomIndex: 0,
    stepIndex: 0,
    scores: { R: 0, I: 0, A: 0, S: 0, E: 0, C: 0 },
    puzzleCorrect: 0,
    wrongAttempts: 0,
    startedAt: Date.now(),
    actions: [],
    completed: false,
    saving: false,
    saved: false,
    saveError: '',
    advice: null
  };
}

async function loadBackendGameId() {
  try {
    const games = await getMiniGames() || [];
    return (games.find(game => game.code === 'data-detective') || games[0])?.id || '';
  } catch {
    return '';
  }
}

export async function minigamePage() {
  state = initialState(await loadBackendGameId());
  return renderShell();
}

function currentRoom() {
  return escapeRooms[state.roomIndex];
}

function currentStep() {
  return currentRoom().steps[state.stepIndex];
}

function progressPercent() {
  const done = state.roomIndex * 5 + state.stepIndex;
  return Math.round(done / (escapeRooms.length * 5) * 100);
}

function renderShell() {
  return `<section class="escape-game" data-escape-root>${renderGame()}</section>`;
}

function renderGame(message = '') {
  if (state.completed) return renderResult();
  const room = currentRoom();
  const step = currentStep();
  const progress = progressPercent();
  const roomTabs = escapeRooms.map((item, index) => `<button class="${index === state.roomIndex ? 'active' : ''}" disabled><b>${item.id}</b><span>${escapeHtml(item.title.replace('Phòng ', ''))}</span></button>`).join('');
  const choices = step.choices.map(([label, score], index) => `<button class="escape-choice" data-escape-answer="${index}" data-score="${score}"><span>${letters[index]}</span><strong>${escapeHtml(label)}</strong></button>`).join('');
  return `
    <div class="escape-hero">
      <div>
        <span class="eyebrow">RIASEC ESCAPE ROOM</span>
        <h2>Thoát khỏi 6 căn phòng nghề nghiệp</h2>
        <p>Đi qua các tình huống ngắn để đo thiên hướng nghề nghiệp. Câu đố phải trả lời đúng mới mở được màn tiếp theo.</p>
      </div>
      <div class="escape-clock"><strong>${progress}%</strong><span>tiến trình</span></div>
    </div>
    <div class="escape-layout">
      <aside class="escape-map card">
        <div class="card-head"><h2>${icon('route', 17)} Bản đồ phòng</h2></div>
        <div class="escape-tabs">${roomTabs}</div>
        <div class="escape-score-mini">${renderMiniScores()}</div>
      </aside>
      <main class="escape-stage card">
        <div class="escape-stage-top">
          <div>
            <span class="pill">${room.type} - ${room.id}</span>
            <h2>${escapeHtml(room.title)}</h2>
            <p>${escapeHtml(room.focus)}</p>
          </div>
          <span class="escape-step">${state.stepIndex + 1}/5</span>
        </div>
        ${renderScene(room, step)}
        <div class="escape-story">
          <span>${step.kind === 'puzzle' ? 'Câu đố mở khóa' : 'Tình huống định hướng'}</span>
          <h3>${escapeHtml(step.title)}</h3>
          <p>${escapeHtml(step.text)}</p>
          ${message ? `<div class="escape-message">${escapeHtml(message)}</div>` : ''}
        </div>
        <div class="escape-choices">${choices}</div>
      </main>
    </div>`;
}

function renderMiniScores() {
  return Object.entries(state.scores).map(([key, value]) => `<label><span>${key} - ${riasecLabels[key]}</span><b>${value}</b><i style="width:${Math.min(100, value / 9 * 100)}%"></i></label>`).join('');
}

function renderScene(room, step) {
  const sceneNote = step.kind === 'puzzle' ? step.title : room.subtitle;
  const folder = roomImageFolders[state.roomIndex] || `R${state.roomIndex + 1}`;
  const stepNumber = state.stepIndex + 1;
  const fileName = roomImageFileOverrides[`${folder}-${stepNumber}`] || `${folder}M${stepNumber}.png`;
  const imageSrc = `../images/${folder}/${fileName}`;

  return `
    <div class="escape-scene scene-${room.scene}">
      <img class="escape-scene-image" src="${imageSrc}" alt="${escapeHtml(room.title)} - ${escapeHtml(step.title)}">
      <div class="scene-image-shade"></div>
      <div class="scene-label">${escapeHtml(sceneNote)}</div>
    </div>`;

  const layers = {
    R: `
      <div class="scene-grid"></div>
      <div class="scene-glow"></div>
      <div class="scene-door"></div>
      <div class="scene-alarm"></div>
      <div class="scene-toolbox"></div>
      <div class="scene-cable scene-cable-a"></div>
      <div class="scene-cable scene-cable-b"></div>
      <div class="scene-bolt"></div>
      <div class="scene-meter"></div>
      <div class="scene-label">${escapeHtml(sceneNote)}</div>`,
    I: `
      <div class="scene-grid"></div>
      <div class="scene-glow"></div>
      <div class="scene-door"></div>
      <div class="scene-beaker beaker-a"></div>
      <div class="scene-beaker beaker-b"></div>
      <div class="scene-microscope"></div>
      <div class="scene-formula">3 → 6 → 12 → ?</div>
      <div class="scene-ph">pH</div>
      <div class="scene-label">${escapeHtml(sceneNote)}</div>`,
    A: `
      <div class="scene-grid"></div>
      <div class="scene-glow"></div>
      <div class="scene-door"></div>
      <div class="scene-canvas"></div>
      <div class="scene-piano"></div>
      <div class="scene-palette"></div>
      <div class="scene-notes"></div>
      <div class="scene-ribbon"></div>
      <div class="scene-label">${escapeHtml(sceneNote)}</div>`,
    S: `
      <div class="scene-grid"></div>
      <div class="scene-glow"></div>
      <div class="scene-door"></div>
      <div class="scene-person"></div>
      <div class="scene-aidkit"></div>
      <div class="scene-bubble bubble-a"></div>
      <div class="scene-bubble bubble-b"></div>
      <div class="scene-bandage"></div>
      <div class="scene-label">${escapeHtml(sceneNote)}</div>`,
    E: `
      <div class="scene-grid"></div>
      <div class="scene-glow"></div>
      <div class="scene-door"></div>
      <div class="scene-table"></div>
      <div class="scene-robot"></div>
      <div class="scene-arrow arrow-a"></div>
      <div class="scene-arrow arrow-b"></div>
      <div class="scene-chart"></div>
      <div class="scene-label">${escapeHtml(sceneNote)}</div>`,
    C: `
      <div class="scene-grid"></div>
      <div class="scene-glow"></div>
      <div class="scene-door"></div>
      <div class="scene-cabinet"></div>
      <div class="scene-files"></div>
      <div class="scene-checklist"></div>
      <div class="scene-magnifier"></div>
      <div class="scene-calc"></div>
      <div class="scene-label">${escapeHtml(sceneNote)}</div>`
  }[room.id] || '';
  return `<div class="escape-scene scene-${room.scene}">${layers}</div>`;
}

function handleAnswer(choiceIndex) {
  const room = currentRoom();
  const step = currentStep();
  const [label, score] = step.choices[choiceIndex];
  const action = {
    roomId: room.id,
    roomCode: room.code,
    stepIndex: state.stepIndex + 1,
    stepKind: step.kind,
    choice: letters[choiceIndex],
    label,
    score
  };
  state.actions.push(action);

  if (step.kind === 'puzzle') {
    if (score === 0) {
      state.wrongAttempts += 1;
      updateGame('Sai rồi, hãy quan sát lại manh mối và chọn đáp án khác.');
      return;
    }
    state.puzzleCorrect += 1;
  } else {
    state.scores[room.id] += score;
  }

  advance(step.reward || '');
}

function advance(message) {
  if (state.stepIndex < currentRoom().steps.length - 1) {
    state.stepIndex += 1;
    updateGame(message);
    return;
  }
  if (state.roomIndex < escapeRooms.length - 1) {
    state.roomIndex += 1;
    state.stepIndex = 0;
    updateGame('Cửa đã mở. Bạn bước sang căn phòng tiếp theo.');
    return;
  }
  state.completed = true;
  updateGame();
  void saveResult();
}

function topTypes() {
  return Object.entries(state.scores).sort((a, b) => b[1] - a[1]).slice(0, 3);
}

function renderResult() {
  const tops = topTypes();
  const max = Math.max(1, ...Object.values(state.scores));
  const durationSeconds = Math.max(1, Math.round((Date.now() - state.startedAt) / 1000));
  const bars = Object.entries(state.scores).map(([key, value]) => `<label><span><b>${key}</b>${riasecLabels[key]}</span><i><em style="width:${value / max * 100}%"></em></i><strong>${value}</strong></label>`).join('');
  const saveLine = !isAuthenticated()
    ? '<p class="muted">Bạn đang chơi ở chế độ khách. Đăng nhập để lưu kết quả vào backend.</p><a class="btn btn-primary" href="login.html">Đăng nhập để lưu</a>'
    : state.saved
      ? '<p class="escape-save ok">Đã lưu kết quả và tạo gợi ý AI từ dữ liệu việc làm.</p>'
      : state.saveError
        ? `<p class="escape-save error">${escapeHtml(state.saveError)}</p>`
        : '<p class="escape-save">Đang lưu kết quả và tạo gợi ý AI...</p>';
  return `
    <div class="escape-result">
      <div class="escape-hero result">
        <div>
          <span class="eyebrow">HOÀN THÀNH ESCAPE ROOM</span>
          <h2>Mã định hướng nổi bật: ${tops.map(([key]) => key).join(' - ')}</h2>
          <p>Bạn đã vượt qua 6 phòng. Kết quả dưới đây phản ánh lựa chọn tình huống và độ chính xác khi giải đố.</p>
        </div>
        <div class="escape-clock"><strong>${state.puzzleCorrect}/${totalPuzzles}</strong><span>câu đố đúng</span></div>
      </div>
      <div class="escape-result-grid">
        ${card('Biểu đồ RIASEC', `<div class="escape-bars">${bars}</div>`, { icon: 'chart' })}
        ${card('Tóm tắt', `<div class="escape-summary"><h3>${tops.map(([key]) => `${key} - ${riasecLabels[key]}`).join(', ')}</h3><p>Thời gian chơi: ${durationSeconds} giây. Số lần chọn sai câu đố: ${state.wrongAttempts}.</p>${saveLine}<button class="btn btn-outline btn-block" data-escape-restart>Chơi lại ${icon('arrow', 15)}</button></div>`, { icon: 'trophy' })}
      </div>
      ${renderAiAdvice()}
    </div>`;
}

function renderAiAdvice() {
  const advice = state.advice;
  if (!advice) return '';
  const careers = (advice.careerOptions || []).slice(0, 3).map((career, index) => `
    <article class="recommend-card compact">
      <div class="recommend-top"><span class="best-label">Lựa chọn #${index + 1}</span><span class="match">${Math.round(Number(career.score || 0))}% phù hợp</span></div>
      <h3>${escapeHtml(career.titleVi || career.titleEn || 'Nghề nghiệp')}</h3>
      <div class="tags"><span>${escapeHtml(career.recommendationGroup || 'AI')}</span><span>${escapeHtml(career.onetCode || '')}</span></div>
      <p>${escapeHtml(career.reason || 'LLM đã phân tích từ kết quả mini-game và dữ liệu việc làm hiện có.')}</p>
    </article>`).join('');
  const jobs = (advice.marketJobs || []).slice(0, 4).map(job => `
    <li>
      <b>${escapeHtml(job.title || 'Job sample TopCV')}</b>
      <span>${escapeHtml(job.location || job.region || 'Không rõ khu vực')} · ${salaryText(job)}</span>
    </li>`).join('');
  const nextSteps = (advice.nextSteps || []).slice(0, 4).map(step => `<li>${icon('check', 15)} ${escapeHtml(step)}</li>`).join('');
  const content = `
    <div class="ai-result">
      <div class="ai-result-copy">
        <span class="eyebrow">LLM CAREER DISCOVERY</span>
        <h3>Khám phá nghề nghiệp từ mini-game</h3>
        <p>${escapeHtml(advice.content || 'AI đã tạo gợi ý từ dữ liệu mini-game và jobs.csv.')}</p>
        <div class="tags">${(advice.topRiasecTypes || []).slice(0, 3).map(type => `<span>${escapeHtml(type.split(':')[0])}</span>`).join('')}</div>
      </div>
      <div class="ai-result-actions">
        <a class="btn btn-primary" href="recommendations.html">Xem gợi ý cá nhân hóa ${icon('arrow', 15)}</a>
        <a class="btn btn-outline" href="learning.html">Mở lộ trình học tập</a>
        <a class="btn btn-ghost" href="careers.html">Khám phá nghề phù hợp</a>
      </div>
    </div>
    <div class="escape-result-grid ai-grid">
      ${card('Nghề nên khám phá', `<div class="ai-career-options">${careers || '<p class="muted">Chưa có nghề phù hợp.</p>'}</div>`, { icon: 'briefcase' })}
      ${card('Job sample từ data/jobs.csv', `<ul class="profile-list">${jobs || '<li>Chưa tìm thấy job sample phù hợp.</li>'}</ul>`, { icon: 'info' })}
      ${card('Bước tiếp theo', `<ul class="check-list">${nextSteps || '<li>Hỏi chatbot để phân tích sâu hơn.</li>'}</ul>`, { icon: 'route' })}
    </div>`;
  return content;
}

function salaryText(job = {}) {
  const min = Number(job.salaryMin || 0);
  const max = Number(job.salaryMax || 0);
  if (min && max) return `${min} - ${max} triệu`;
  if (min) return `từ ${min} triệu`;
  if (max) return `đến ${max} triệu`;
  return job.sourceName || 'TopCV CSV';
}

async function saveResult() {
  if (!isAuthenticated() || !state.backendGameId || state.saving || state.saved) return;
  state.saving = true;
  try {
    const session = await startMiniGame(state.backendGameId);
    await recordMiniGameActions(session.sessionId, state.actions.map(action => ({
      actionType: action.stepKind,
      actionPayload: action
    })));
    const durationSeconds = Math.max(60, Math.round((Date.now() - state.startedAt) / 1000));
    const accuracy = totalPuzzles ? state.puzzleCorrect / totalPuzzles : 0;
    const result = await completeMiniGame(session.sessionId, {
      accuracy,
      durationSeconds,
      answerChanges: state.wrongAttempts
    }, JSON.stringify({
      game: 'RIASEC Escape Room',
      scores: state.scores,
      topTypes: topTypes().map(([key]) => key),
      puzzleCorrect: state.puzzleCorrect,
      wrongAttempts: state.wrongAttempts
    }));
    state.advice = result.advice || null;
    state.saved = true;
  } catch (error) {
    state.saveError = error.message || 'Chưa lưu được kết quả vào backend.';
  } finally {
    state.saving = false;
    updateGame();
  }
}

function updateGame(message = '') {
  const root = document.querySelector('[data-escape-root]');
  if (!root) return;
  root.innerHTML = renderGame(message);
  bindEscapeEvents();
}

function bindEscapeEvents() {
  document.querySelectorAll('[data-escape-answer]').forEach(button => button.addEventListener('click', () => handleAnswer(Number(button.dataset.escapeAnswer))));
  document.querySelector('[data-escape-restart]')?.addEventListener('click', () => {
    state = initialState(state.backendGameId);
    updateGame();
  });
}

export function bindMinigameEvents() {
  bindEscapeEvents();
}
