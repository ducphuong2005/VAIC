import { careerGrid } from '../components/widgets.js';
import { careers } from '../data.js';
import { icon } from '../components/icons.js';

export function favoritesPage(){
 return `<div class="favorite-tabs tabs"><button class="active" data-fav-tab="career">Nghề nghiệp <span>3</span></button><button data-fav-tab="course">Khóa học <span>2</span></button><button data-fav-tab="article">Bài viết <span>4</span></button></div><div class="saved-toolbar"><div><h2>Nghề nghiệp đã lưu</h2><p>So sánh và tìm hiểu kỹ hơn trước khi đưa ra lựa chọn.</p></div><button class="btn btn-outline">${icon('chart',16)} So sánh nghề</button></div><div id="favorite-content">${careerGrid(careers.slice(0,3))}</div>`;
}
