export const navItems = [
  ['home', 'Trang chủ', 'index.html', 'home'],
  ['profile', 'Hồ sơ của bạn', 'profile.html', 'user'],
  ['minigame', 'Mini-game', 'minigame.html', 'game'],
  ['careers', 'Khám phá nghề', 'careers.html', 'briefcase'],
  ['learning', 'Lộ trình học tập', 'learning.html', 'route'],
  ['favorites', 'Yêu thích', 'favorites.html', 'heart'],
  ['settings', 'Cài đặt', 'settings.html', 'settings']
];

export const pageMeta = {
  home: user => [`Xin chào, ${(user?.fullName || user?.name || 'bạn').trim()}!`, 'Khám phá điểm mạnh và nghề nghiệp phù hợp với bạn.'],
  profile: ['Hồ sơ của bạn', 'Quản lý thông tin và theo dõi mức độ hoàn thiện hồ sơ.'],
  minigame: ['Mini-game nghề nghiệp', 'Vừa chơi vừa khám phá những kỹ năng tiềm ẩn của bạn.'],
  careers: ['Khám phá nghề nghiệp', 'Tìm hiểu cơ hội, mức lương và xu hướng tuyển dụng mới nhất.'],
  learning: ['Lộ trình học tập', 'Từng bước trang bị kỹ năng để tiến gần hơn đến nghề nghiệp mục tiêu.'],
  favorites: ['Danh sách yêu thích', 'Những nghề nghiệp và khóa học bạn đã lưu.'],
  settings: ['Cài đặt', 'Cá nhân hóa trải nghiệm và quản lý tài khoản của bạn.']
};
