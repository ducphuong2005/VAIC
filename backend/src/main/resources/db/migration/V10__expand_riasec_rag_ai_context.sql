INSERT INTO onet_elements (element_id, element_name, category, description) VALUES
('RIASEC-R', 'Realistic Interest', 'INTEREST', 'Thích hoạt động thực hành, công cụ, máy móc hoặc thao tác cụ thể.'),
('RIASEC-A', 'Artistic Interest', 'INTEREST', 'Thích sáng tạo, biểu đạt và môi trường ít khuôn mẫu.'),
('RIASEC-S', 'Social Interest', 'INTEREST', 'Thích hỗ trợ, giảng dạy, chữa lành hoặc tư vấn cho người khác.'),
('RIASEC-E', 'Enterprising Interest', 'INTEREST', 'Thích dẫn dắt, thuyết phục và quản lý để đạt mục tiêu.');

INSERT INTO occupation_element_scores (onet_code, element_id, category, score) VALUES
('15-2051.00', 'RIASEC-R', 'INTEREST', 45),
('15-2051.00', 'RIASEC-A', 'INTEREST', 58),
('15-2051.00', 'RIASEC-S', 'INTEREST', 52),
('15-2051.00', 'RIASEC-E', 'INTEREST', 60),
('15-1252.00', 'RIASEC-R', 'INTEREST', 55),
('15-1252.00', 'RIASEC-C', 'INTEREST', 70),
('15-1252.00', 'RIASEC-A', 'INTEREST', 68),
('15-1252.00', 'RIASEC-S', 'INTEREST', 48),
('15-1252.00', 'RIASEC-E', 'INTEREST', 54),
('13-1161.00', 'RIASEC-R', 'INTEREST', 38),
('13-1161.00', 'RIASEC-I', 'INTEREST', 72),
('13-1161.00', 'RIASEC-A', 'INTEREST', 70),
('13-1161.00', 'RIASEC-S', 'INTEREST', 64),
('13-1161.00', 'RIASEC-E', 'INTEREST', 82);

INSERT INTO rag_documents (title, content, document_type, onet_code, language, region, source_version, active, created_at, updated_at) VALUES
('RIASEC reference', 'RIASEC gồm sáu nhóm sở thích nghề nghiệp: Realistic thích thực hành và công cụ; Investigative thích phân tích và nghiên cứu; Artistic thích sáng tạo; Social thích giúp đỡ và tư vấn; Enterprising thích dẫn dắt và thuyết phục; Conventional thích dữ liệu, hồ sơ, chi tiết và quy trình rõ ràng.', 'FAQ', NULL, 'vi', NULL, 'demo-v2', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TopCV market sample limitation', 'Các job crawl từ TopCV trong Career Compass chỉ là mẫu tín hiệu thị trường. Hệ thống dùng chúng để tham khảo nhu cầu kỹ năng, vùng tuyển dụng và loại vị trí, không xem là toàn bộ thị trường lao động Việt Nam.', 'MARKET_REPORT', NULL, 'vi', 'Vietnam', 'demo-v2', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
