import { careerGrid } from '../components/widgets.js';
import { icon } from '../components/icons.js';
import { careers } from '../data.js';

export function careersPage(){
 const hero=`<div class="career-search-hero"><span class="eyebrow">KHÁM PHÁ HƠN 200 NGHỀ NGHIỆP</span><h2>Tìm công việc khiến bạn <em>muốn thức dậy mỗi sáng</em></h2><div class="big-search">${icon('search')}<input data-career-search placeholder="Tìm theo tên nghề nghiệp, kỹ năng..."><button class="btn btn-primary">Tìm kiếm</button></div><div class="popular-search">Phổ biến: <button>Data Analyst</button><button>UI/UX Designer</button><button>Marketing</button></div></div>`;
 const toolbar=`<div class="browse-head"><div><h2>Nghề nghiệp dành cho bạn</h2><p>Dựa trên hồ sơ và xu hướng thị trường</p></div><div class="filter-controls"><select data-career-category><option value="all">Tất cả nhóm ngành</option><option value="data">Dữ liệu & Công nghệ</option><option value="creative">Sáng tạo</option></select><select><option>Phù hợp nhất</option><option>Lương cao nhất</option></select></div></div>`;
 return `${hero}<div class="category-chips" data-career-chips><button class="active">Tất cả</button><button>💻 Công nghệ</button><button>📊 Kinh tế</button><button>🎨 Sáng tạo</button><button>🩺 Y tế</button><button>📚 Giáo dục</button></div>${toolbar}${careerGrid(careers)}<button class="btn btn-outline load-more" data-load-more>Xem thêm nghề nghiệp</button>`;
}
