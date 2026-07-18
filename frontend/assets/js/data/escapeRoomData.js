export const riasecLabels = {
  R: 'Kỹ thuật',
  I: 'Nghiên cứu',
  A: 'Nghệ thuật',
  S: 'Xã hội',
  E: 'Quản lý',
  C: 'Nghiệp vụ'
};

export const escapeRooms = [
  {
    id: 'R',
    code: 'technical-station',
    type: 'REALISTIC',
    title: 'Phòng Kỹ thuật',
    subtitle: 'Trạm phát điện ngầm',
    focus: 'Thao tác, tư duy cơ khí, công cụ vật lý',
    minutes: 8,
    scene: 'station',
    steps: [
      {
        kind: 'trait',
        title: 'Tiếp cận bối cảnh',
        text: 'Bạn tỉnh dậy trong một trạm phát điện ngầm ngột ngạt. Đèn báo động đỏ nhấp nháy, oxy đang giảm nhanh. Bạn sẽ làm gì đầu tiên?',
        choices: [
          ['Tìm kiếm hộp dụng cụ kỹ thuật xung quanh phòng.', 3],
          ['Đi vòng quanh kiểm tra hệ thống dây cáp nối vào cửa ra vào.', 2],
          ['Đứng trước bảng điều khiển chính, cố tìm nút reset hệ thống.', 1]
        ]
      },
      {
        kind: 'puzzle',
        title: 'Tìm kiếm công cụ',
        text: 'Bạn phát hiện một chiếc hòm sắt bị kẹt. Trên nắp hòm có câu đố: Hình lục giác có bao nhiêu cạnh?',
        reward: 'Đúng, hòm mở ra. Bạn nhặt được kìm và tua vít.',
        choices: [['5 cạnh', 0], ['6 cạnh', 2], ['8 cạnh', 0]]
      },
      {
        kind: 'trait',
        title: 'Thao tác kỹ thuật',
        text: 'Có dụng cụ trong tay, bạn tiếp cận tủ điện chính đang bốc khói. Bạn xử lý thế nào?',
        choices: [
          ['Dùng kìm cách điện cắt phăng dây nguồn đang chập cháy.', 3],
          ['Dùng tua vít tháo lớp vỏ bảo vệ để kiểm tra cầu chì bên trong.', 2],
          ['Lấy một thanh gỗ khô gần đó gạt cần gạt tổng xuống.', 1]
        ]
      },
      {
        kind: 'puzzle',
        title: 'Sửa chữa mạch điện',
        text: 'Mạch điện ghi I = U / R. Để giảm cường độ dòng điện I bảo vệ mạch, bạn phải làm gì với điện trở R?',
        reward: 'Đúng, mạch điện ổn định trở lại.',
        choices: [['Tăng điện trở R', 2], ['Giảm điện trở R', 0], ['Giữ nguyên điện trở R', 0]]
      },
      {
        kind: 'trait',
        title: 'Kích hoạt lối thoát',
        text: 'Máy phát điện đã chạy lại. Cửa điện mở hé nhưng bị kẹt bởi một thanh sắt lớn chặn ngang. Hành động cuối cùng của bạn?',
        choices: [
          ['Dùng xà beng làm đòn bẩy để bẩy mạnh thanh sắt ra.', 3],
          ['Bôi một ít dầu máy vào khe kẹt rồi từ từ đẩy cửa.', 2],
          ['Gọi to xem có ai ở ngoài nghe thấy để kéo hộ không.', 1]
        ]
      }
    ]
  },
  {
    id: 'I',
    code: 'bio-lab',
    type: 'INVESTIGATIVE',
    title: 'Phòng Nghiên cứu',
    subtitle: 'Phòng thí nghiệm hóa sinh',
    focus: 'Logic, phân tích dữ liệu, giải quyết vấn đề',
    minutes: 9,
    scene: 'lab',
    steps: [
      { kind: 'trait', title: 'Tiếp cận bối cảnh', text: 'Bạn bước vào một phòng thí nghiệm hóa sinh bảo mật cao. Trên bàn đầy tài liệu mã hóa và ống nghiệm. Bạn bắt đầu từ đâu?', choices: [['Mở nhật ký nghiên cứu của giáo sư để đọc ghi chép.', 3], ['Quan sát các phương trình hóa học trên bảng tường.', 2], ['Gõ thử vào các bể kính nuôi cấy xem có sinh vật nào không.', 1]] },
      { kind: 'puzzle', title: 'Giải mã chuỗi logic', text: 'Để kích hoạt máy tính trung tâm, bạn phải điền số tiếp theo vào dãy: 3, 6, 12, 24, ...', reward: 'Đúng, máy tính trung tâm bật mở.', choices: [['36', 0], ['48', 2], ['30', 0]] },
      { kind: 'trait', title: 'Nghiên cứu thực nghiệm', text: 'Máy tính hiển thị: Cần trung hòa dung dịch axit X để lấy chìa khóa ở đáy cốc. Bạn làm gì?', choices: [['Dùng quỳ tím thử pH rồi nhỏ từ từ dung dịch bazơ.', 3], ['Đổ một lượng lớn chất kiềm vào và chờ phản ứng kết thúc.', 2], ['Đeo găng dày và thò tay lấy thẳng chìa khóa.', 1]] },
      { kind: 'puzzle', title: 'Phân tích thành phần', text: 'Chất nào sau đây có tính bazơ dùng để trung hòa axit?', reward: 'Đúng, axit được trung hòa an toàn.', choices: [['HCl', 0], ['NaOH', 2], ['H2SO4', 0]] },
      { kind: 'trait', title: 'Kích hoạt lối thoát', text: 'Cửa thoát hiểm yêu cầu từ khóa tiếng Anh: Nghiên cứu khoa học bằng quan sát và thử nghiệm. Từ đó là gì?', choices: [['SCIENCE', 3], ['ACTION', 1], ['THEORY', 2]] }
    ]
  },
  {
    id: 'A',
    code: 'art-gallery',
    type: 'ARTISTIC',
    title: 'Phòng Nghệ thuật',
    subtitle: 'Triển lãm sắc màu',
    focus: 'Tưởng tượng, sáng tạo, cảm xúc',
    minutes: 8,
    scene: 'gallery',
    steps: [
      { kind: 'trait', title: 'Tiếp cận bối cảnh', text: 'Bạn lọt vào căn phòng triển lãm nghệ thuật đầy sắc màu. Tiếng nhạc không lời vang lên từ chiếc đài cổ. Tâm trí bạn bị thu hút bởi điều gì?', choices: [['Bức tranh trừu tượng lớn với những vệt màu đầy cảm xúc.', 3], ['Chiếc đàn piano cổ ở góc phòng đang tự gảy phím.', 2], ['Cách sắp xếp ánh sáng đèn rọi độc đáo trong phòng.', 1]] },
      { kind: 'puzzle', title: 'Cảm thụ âm thanh', text: 'Cánh cửa khóa bằng hộp nhạc. Nốt tiếp theo trong đoạn Đồ - Rê - Mi - Pha - ... là gì?', reward: 'Đúng, hộp nhạc mở ra và rơi ra một mảnh giấy vẽ màu.', choices: [['Son', 2], ['La', 0], ['Si', 0]] },
      { kind: 'trait', title: 'Sáng tạo giải pháp', text: 'Trên bàn có bức tượng đất sét chưa hoàn thiện giữ chìa khóa bên trong. Bạn chọn cách nào?', choices: [['Dùng nước nhào nặn lại tượng thành hình thù mới để giải phóng chìa khóa.', 3], ['Dùng dao khắc từng đường nét dọc thân tượng.', 2], ['Đập vỡ tượng để lấy chìa khóa cho nhanh.', 1]] },
      { kind: 'puzzle', title: 'Phối màu nghệ thuật', text: 'Ổ khóa yêu cầu pha hai màu cơ bản để tạo màu xanh lá cây. Bạn chọn cặp nào?', reward: 'Đúng, ổ khóa đổi màu và mở ra.', choices: [['Đỏ + Vàng', 0], ['Vàng + Xanh lam', 2], ['Đỏ + Xanh lam', 0]] },
      { kind: 'trait', title: 'Kích hoạt lối thoát', text: 'Nếu được vẽ lại căn phòng này theo phong cách của bạn, bạn sẽ chọn tông màu nào?', choices: [['Tông tương phản mạnh bộc lộ cá tính phá cách.', 3], ['Tông nhẹ nhàng, sâu lắng phối hợp nhịp nhàng.', 2], ['Trắng đen tối giản cho gọn gàng.', 1]] }
    ]
  },
  {
    id: 'S',
    code: 'rescue-center',
    type: 'SOCIAL',
    title: 'Phòng Xã hội',
    subtitle: 'Trung tâm cứu hộ khẩn cấp',
    focus: 'Giao tiếp, thấu cảm, hỗ trợ con người',
    minutes: 10,
    scene: 'rescue',
    steps: [
      { kind: 'trait', title: 'Tiếp cận bối cảnh', text: 'Bạn vào trung tâm cứu hộ y tế khẩn cấp. Một NPC bị thương đang hoảng sợ ở góc phòng. Bạn phản ứng thế nào?', choices: [['Ngồi ngang hàng, nhìn vào mắt họ và trấn an.', 3], ['Đi tìm hộp sơ cứu để chuẩn bị băng bó.', 2], ['Hỏi lớn xem họ có biết chìa khóa ở đâu không.', 1]] },
      { kind: 'puzzle', title: 'Sơ cứu tâm lý', text: 'NPC kích động: Tránh ra! Đừng chạm vào tôi. Câu nào giúp xoa dịu tốt nhất?', reward: 'Đúng, NPC bình tĩnh lại và đưa bạn gạc y tế.', choices: [['Tôi chỉ muốn giúp vết thương của anh bớt chảy máu thôi.', 2], ['Anh hét thế thì chúng ta cùng chết ở đây đấy.', 0], ['Im lặng đi để tôi còn tìm cách mở cửa.', 0]] },
      { kind: 'trait', title: 'Chia sẻ và thấu hiểu', text: 'Sau khi được băng bó, NPC kể ông bị kẹt vì quay lại cứu bạn. Bạn nghĩ gì?', choices: [['Cảm động và khẳng định hành động cứu người rất cao đẹp.', 3], ['Đồng cảm và khuyên lần sau cần đảm bảo an toàn bản thân trước.', 2], ['Nghĩ thầm ông ấy liều lĩnh và làm phức tạp vấn đề.', 1]] },
      { kind: 'puzzle', title: 'Kỹ năng lắng nghe', text: 'NPC nói: Mật mã ở dưới gầm tủ... không, tôi nhớ nhầm, nó ở trong túi áo tôi. Vị trí đúng là đâu?', reward: 'Đúng, bạn tìm thấy thẻ từ.', choices: [['Dưới gầm tủ', 0], ['Trong túi áo người đàn ông', 2], ['Trên bàn làm việc', 0]] },
      { kind: 'trait', title: 'Kích hoạt lối thoát', text: 'Cửa mở. NPC quá yếu không tự đi được. Bạn sẽ làm gì?', choices: [['Dìu ông ấy cùng đi ra, chấp nhận di chuyển chậm.', 3], ['Chạy ra ngoài trước để tìm thêm người hỗ trợ.', 2], ['Bảo ông ấy cố tự bò ra theo hướng mình vừa đi.', 1]] }
    ]
  },
  {
    id: 'E',
    code: 'strategy-room',
    type: 'ENTERPRISING',
    title: 'Phòng Quản lý',
    subtitle: 'Phòng họp chiến lược',
    focus: 'Lãnh đạo, thuyết phục, ra quyết định',
    minutes: 9,
    scene: 'strategy',
    steps: [
      { kind: 'trait', title: 'Tiếp cận bối cảnh', text: 'Bạn vào phòng họp chiến lược. Hai NPC cộng sự đang tranh cãi gay gắt về phương án thoát hiểm. Bạn làm gì?', choices: [['Yêu cầu cả hai giữ trật tự và lắng nghe bạn điều phối.', 3], ['Lắng nghe từng người rồi đứng ra làm trung gian hòa giải.', 2], ['Lờ họ đi, tự tìm manh mối riêng.', 1]] },
      { kind: 'puzzle', title: 'Thuyết phục đối tác', text: 'Robot bảo vệ yêu cầu lý do hợp lý để thả nhóm bạn. Lập luận win-win tốt nhất là gì?', reward: 'Đúng, robot bị thuyết phục và mở cổng dữ liệu.', choices: [['Nếu thả chúng tôi, chúng tôi sẽ đánh giá 5 sao cho hệ thống của bạn.', 2], ['Thả ra mau không tôi sẽ đập nát hệ thống.', 0], ['Làm ơn hãy thương hại chúng tôi.', 0]] },
      { kind: 'trait', title: 'Ra quyết định dưới áp lực', text: 'Có hai lối thoát: lối 1 an toàn nhưng mất 30 phút, lối 2 thoát nhanh nhưng rủi ro 50%. Bạn chọn gì?', choices: [['Chọn lối 2, chấp nhận rủi ro lớn để đạt mục tiêu nhanh.', 3], ['Thảo luận nhanh với cộng sự rồi mới chọn.', 2], ['Chọn lối 1 cho chắc chắn.', 1]] },
      { kind: 'puzzle', title: 'Phân chia nguồn lực', text: 'Bạn có một người khỏe mạnh và một người giỏi tính toán. Ai nên đi giải mã bảng điện tử?', reward: 'Đúng, công việc được tối ưu hóa thời gian.', choices: [['Người giỏi tính toán', 2], ['Người khỏe mạnh', 0], ['Tự mình làm hết tất cả', 0]] },
      { kind: 'trait', title: 'Kích hoạt lối thoát', text: 'Cửa chuẩn bị mở. Bạn cần hô khẩu hiệu thúc đẩy cả đội tiến lên. Bạn nói gì?', choices: [['Tất cả đi theo tôi, chúng ta chắc chắn sẽ chiến thắng!', 3], ['Cùng nhau bước qua cánh cửa này an toàn nhé mọi người.', 2], ['Ai chạy được cứ chạy trước đi.', 1]] }
    ]
  },
  {
    id: 'C',
    code: 'archive-office',
    type: 'CONVENTIONAL',
    title: 'Phòng Nghiệp vụ',
    subtitle: 'Văn phòng lưu trữ hồ sơ',
    focus: 'Ngăn nắp, quy trình, chi tiết, số liệu',
    minutes: 10,
    scene: 'archive',
    steps: [
      { kind: 'trait', title: 'Tiếp cận bối cảnh', text: 'Bạn vào văn phòng lưu trữ rộng lớn. Hàng ngàn phong bì nằm rải rác và có bảng nội quy hướng dẫn tìm chìa khóa. Bạn sẽ làm gì?', choices: [['Đọc kỹ bảng nội quy và làm theo từng bước.', 3], ['Gom tài liệu số liệu tài chính để kiểm tra tính chính xác.', 2], ['Lật tung ngẫu nhiên các hộp hồ sơ xem chìa khóa có rơi ra không.', 1]] },
      { kind: 'puzzle', title: 'Sắp xếp dữ liệu', text: 'Sắp xếp tên dự án theo A-Z: Alpha, Gamma, Beta. Thứ tự đúng là gì?', reward: 'Đúng, tủ hồ sơ mở ra một ngăn bí mật.', choices: [['Alpha -> Gamma -> Beta', 0], ['Alpha -> Beta -> Gamma', 2], ['Beta -> Alpha -> Gamma', 0]] },
      { kind: 'trait', title: 'Kiểm toán số liệu', text: 'Trong ngăn bí mật có bảng thu chi tài chính. Việc rà soát con số chi tiết khiến bạn cảm thấy thế nào?', choices: [['Thích thú, tập trung cao độ để tìm sự chuẩn xác giữa các cột số.', 3], ['Bình thường, coi đó là nhiệm vụ cần hoàn thành.', 2], ['Nhàm chán, mệt mỏi vì quá nhiều con số nhỏ.', 1]] },
      { kind: 'puzzle', title: 'Phát hiện lỗi sai', text: 'Tìm kết quả đúng của phép tính: 150 + 250 + 300 = ?', reward: 'Đúng, hệ thống cấp mật mã.', choices: [['700', 2], ['800', 0], ['600', 0]] },
      { kind: 'trait', title: 'Kích hoạt lối thoát', text: 'Cửa cuối đã mở. Để lưu dữ liệu trò chơi an toàn nhất, bạn chọn hành động nào?', choices: [['Nhấn Lưu và Xuất Báo Cáo theo đúng quy trình.', 3], ['Rút thẳng thẻ nhớ dữ liệu ra và cầm theo.', 2], ['Chạy thẳng ra ngoài không cần bấm gì.', 1]] }
    ]
  }
];
