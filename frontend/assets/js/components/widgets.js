import { icon } from './icons.js';
import { careers, skills } from '../data.js';

export function sparkline(color, idx=0) {
  const variants=['2,28 14,12 27,20 40,7 54,13 68,3','2,27 14,16 27,22 40,11 53,17 68,4','2,26 14,18 27,23 41,11 54,14 68,5'];
  return `<svg class="sparkline" viewBox="0 0 70 32"><defs><linearGradient id="g${idx}" x1="0" y1="0" x2="0" y2="1"><stop stop-color="${color}" stop-opacity=".25"/><stop offset="1" stop-color="${color}" stop-opacity="0"/></linearGradient></defs><polygon points="${variants[idx%3]},68,32 2,32" fill="url(#g${idx})"/><polyline points="${variants[idx%3]}" fill="none" stroke="${color}" stroke-width="2"/><circle cx="68" cy="${[3,4,5][idx%3]}" r="2.5" fill="${color}"/></svg>`;
}

export function careerRows(limit=5) {
 return careers.slice(0,limit).map((c,i)=>`<div class="career-row">
  <span class="rank" style="--accent:${c.color}">${c.rank}</span><div class="career-info"><strong>${c.title}</strong><small>${c.subtitle}</small></div>
  ${sparkline(c.color,i)}<div class="salary"><strong>${c.salary}</strong><small>▲ ${c.growth}%</small></div>
 </div>`).join('');
}

export function skillStrip() {
 return `<div class="skills-strip">${skills.map(([name,value,emoji])=>`<div class="skill-mini"><span>${emoji}</span><div><strong>${name}</strong><small>${value}%</small><div class="mini-progress"><i style="width:${value}%"></i></div></div></div>`).join('')}</div>`;
}

export function recommendationCard(compact=false) {
 return `<div class="recommend-card ${compact?'compact':''}"><div class="recommend-top"><span class="best-label">Phù hợp nhất với bạn</span><span class="match">92% phù hợp</span></div><h3>Khoa học dữ liệu <span>(Data Science)</span></h3><div class="tags"><span>Phân tích dữ liệu</span><span>Machine Learning</span><span>Giải quyết vấn đề</span></div><h4>Vì sao phù hợp?</h4><ul class="check-list"><li>${icon('check',16)} Kết quả mini-game cho thấy bạn có tư duy phân tích và xử lý số liệu tốt.</li><li>${icon('check',16)} Bạn yêu thích giải quyết vấn đề và tìm ra quy luật.</li><li>${icon('check',16)} Thị trường đang có nhu cầu cao và thu nhập hấp dẫn.</li></ul><a href="recommendations.html" class="text-link">Xem giải thích chi tiết ${icon('arrow',15)}</a></div>`;
}

export function careerGrid(items=careers) {
 return `<div class="career-grid">${items.map((c,i)=>`<article class="career-card" data-career="${c.title.toLowerCase()}"><div class="career-cover cover-${i%4}"><span>${['⌁','◫','✦','◎'][i%4]}</span><button class="save-btn" aria-label="Lưu nghề" data-save>${icon('bookmark',18)}</button></div><div class="career-body"><div class="match-line"><span>${c.tags[0]}</span><strong>${c.match}% phù hợp</strong></div><h3>${c.title}</h3><p>${c.subtitle}</p><div class="career-stats"><span>💰 ${c.salary}</span><span class="up">▲ ${c.growth}%</span></div><a class="btn btn-soft btn-block" href="recommendations.html">Xem chi tiết ${icon('arrow',15)}</a></div></article>`).join('')}</div>`;
}
