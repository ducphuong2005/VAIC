import { card } from '../components/layout.js';
import { icon } from '../components/icons.js';

const activities=[['Hôm nay','Hoàn thành mini-game “Data Detective”','Bạn đạt 9,240 điểm và mở khóa huy hiệu Bậc thầy dữ liệu.','game','16:20'],['Hôm nay','Đã lưu nghề “Khoa học dữ liệu”','Nghề này có mức độ phù hợp 92% với hồ sơ của bạn.','bookmark','10:45'],['Hôm qua','Tiếp tục bài đánh giá tính cách','Tiến độ hiện tại: 65% · Còn khoảng 3 phút.','discover','20:12'],['15/07/2026','Trò chuyện với Career Compass AI','Chủ đề: So sánh Data Science và Business Analyst.','bot','14:30'],['12/07/2026','Cập nhật thông tin hồ sơ','Đã thêm trường học, chuyên ngành và khu vực mong muốn.','user','09:15']];

export function historyPage(){
 const filters=`<div class="history-filter"><div class="tabs"><button class="active">Tất cả</button><button>Đánh giá</button><button>Mini-game</button><button>AI Chat</button></div><select><option>30 ngày gần nhất</option><option>7 ngày gần nhất</option></select></div>`;
 const timeline=`<div class="activity-timeline">${activities.map(([date,title,desc,ico,time],i)=>`<article><div class="activity-icon ai-${i%4}">${icon(ico,19)}</div><div><span class="activity-date">${date} · ${time}</span><h3>${title}</h3><p>${desc}</p></div><button class="icon-btn">${icon('chevron',17)}</button></article>`).join('')}</div>`;
 return `${filters}<div class="history-layout"><div>${card('Hoạt động gần đây',timeline,{icon:'clock'})}</div><aside>${card('Thống kê tháng 7','<div class="history-stats"><div><strong>12</strong><span>Hoạt động</span></div><div><strong>3h 45m</strong><span>Thời gian học</span></div><div><strong>4</strong><span>Kỹ năng mới</span></div></div>',{icon:'chart'})}${card('Dữ liệu của bạn','<p class="muted">Bạn có thể tải xuống toàn bộ lịch sử hoạt động bất cứ lúc nào.</p><button class="btn btn-outline btn-block">'+icon('download',16)+' Xuất dữ liệu</button>',{})}</aside></div>`;
}
