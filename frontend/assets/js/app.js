import { layout } from './components/layout.js';
import { homePage } from './pages/home.js';
import { profilePage } from './pages/profile.js';
import { discoveryPage } from './pages/discovery.js';
import { minigamePage } from './pages/minigame.js';
import { careersPage } from './pages/careers.js';
import { recommendationsPage } from './pages/recommendations.js';
import { learningPage } from './pages/learning.js';
import { favoritesPage } from './pages/favorites.js';
import { historyPage } from './pages/history.js';
import { settingsPage } from './pages/settings.js';

const page=document.body.dataset.page||'home';
const pages={home:homePage,profile:profilePage,discovery:discoveryPage,minigame:minigamePage,careers:careersPage,recommendations:recommendationsPage,learning:learningPage,favorites:favoritesPage,history:historyPage,settings:settingsPage};
document.querySelector('#app').innerHTML=layout(page,(pages[page]||homePage)());

const $=(s,root=document)=>root.querySelector(s), $$=(s,root=document)=>[...root.querySelectorAll(s)];
const toast=(text)=>{const el=$('#toast');el.textContent=text;el.classList.add('show');setTimeout(()=>el.classList.remove('show'),2200)};

$('[data-sidebar-open]')?.addEventListener('click',()=>document.body.classList.add('sidebar-open'));
$$('[data-sidebar-close], .nav-item').forEach(x=>x.addEventListener('click',()=>document.body.classList.remove('sidebar-open')));

$$('[data-tabs]').forEach(group=>$$('button',group).forEach(btn=>btn.addEventListener('click',()=>{$$('button',group).forEach(x=>x.classList.remove('active'));btn.classList.add('active')})));

$$('input[data-max]').forEach(input=>input.addEventListener('change',()=>{const checked=$$('input[data-max]:checked');if(checked.length>3){input.checked=false;toast('Bạn chỉ có thể chọn tối đa 3 đáp án.')}}));
$('[data-next]')?.addEventListener('click',()=>toast('Đã lưu câu trả lời. Chuyển sang bước 3/6!'));

$('[data-chat-form]')?.addEventListener('submit',e=>{e.preventDefault();const input=$('input',e.currentTarget);if(!input.value.trim())return;const box=$('.chat-box');box.insertAdjacentHTML('beforeend',`<div class="message me">${input.value.replace(/[<>]/g,'')}<time>Bây giờ ✓✓</time></div>`);input.value='';box.scrollTop=box.scrollHeight;setTimeout(()=>{box.insertAdjacentHTML('beforeend','<div class="message bot">Mình đã ghi nhận. Bạn có thể mở trang Gợi ý cá nhân hóa để xem phân tích chi tiết nhé! 😊<time>Bây giờ</time></div>');box.scrollTop=box.scrollHeight},500)});

$$('[data-save]').forEach(btn=>btn.addEventListener('click',()=>{btn.classList.toggle('saved');toast(btn.classList.contains('saved')?'Đã lưu vào danh sách yêu thích':'Đã bỏ khỏi danh sách yêu thích')}));

const search=$('[data-career-search]');
search?.addEventListener('input',()=>{$$('.career-card').forEach(card=>card.hidden=!card.dataset.career.includes(search.value.trim().toLowerCase()))});
$$('[data-career-chips] button').forEach(btn=>btn.addEventListener('click',()=>{$$('[data-career-chips] button').forEach(x=>x.classList.remove('active'));btn.classList.add('active');toast(`Đang hiển thị: ${btn.textContent.trim()}`)}));
$('[data-load-more]')?.addEventListener('click',e=>{e.currentTarget.textContent='Đã hiển thị tất cả nghề nghiệp';e.currentTarget.disabled=true});

$$('[data-game-filter] button').forEach(btn=>btn.addEventListener('click',()=>{const filter=btn.dataset.filter;$$('[data-game-filter] button').forEach(x=>x.classList.remove('active'));btn.classList.add('active');$$('.game-tile').forEach(x=>x.hidden=filter!=='all'&&x.dataset.gameCategory!==filter)}));
$$('[data-test-start]').forEach(btn=>btn.addEventListener('click',()=>toast('Bài đánh giá đã sẵn sàng. Chúc bạn có trải nghiệm thú vị!')));

$$('[data-fav-tab]').forEach(btn=>btn.addEventListener('click',()=>{$$('[data-fav-tab]').forEach(x=>x.classList.remove('active'));btn.classList.add('active');if(btn.dataset.favTab!=='career')$('#favorite-content').innerHTML=`<div class="empty-state"><h3>Nội dung đang được cập nhật</h3><p>Danh sách ${btn.textContent.trim()} của bạn sẽ xuất hiện tại đây.</p></div>`}));

$$('[data-setting]').forEach(btn=>btn.addEventListener('click',()=>{$$('[data-setting]').forEach(x=>x.classList.remove('active'));btn.classList.add('active');if(btn.dataset.setting!=='account')toast(`Đã chuyển sang mục ${btn.textContent.trim()}`)}));

document.addEventListener('click',e=>{const button=e.target.closest('.btn-primary:not([data-next]), .btn-outline');if(button&&button.tagName==='BUTTON'&&!button.closest('form')&&!button.hasAttribute('data-test-start')){if(!button.closest('[data-load-more]')) toast('Thao tác đã được ghi nhận!')}});
