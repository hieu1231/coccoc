package com.test.coccoc.data.datasource

object MockArticleData {

    // Audio URLs are now found dynamically via JavaScript injection in WebView
    // This allows the app to work with real article pages from VnExpress and Dân Trí

    val articlesJson = """
    [
        {
            "id": "1",
            "title": "Podcast: Bí mật hoa hồng bán ô tô hạng sang",
            "thumbnailUrl": "https://picsum.photos/seed/article1/680/408",
            "contentSnippet": "Nguyễn Quang Đức, có 10 năm kinh nghiệm kinh doanh xe hơi hạng sang, chia sẻ những yếu tố then chốt giúp một nhân viên bán hàng lấy được thiện cảm với khách hàng.",
            "fullContent": "Nguyễn Quang Đức, có 10 năm kinh nghiệm kinh doanh xe hơi hạng sang, chia sẻ những yếu tố then chốt giúp một nhân viên bán hàng lấy được thiện cảm với khách hàng trong số podcast chủ đề 'Nghề hot'.\n\nTheo anh Đức, việc bán xe hạng sang không chỉ đơn thuần là giao dịch mua bán, mà còn là nghệ thuật xây dựng mối quan hệ với khách hàng. Những khách hàng mua xe cao cấp thường có yêu cầu rất cao về chất lượng dịch vụ và sự chuyên nghiệp.\n\nMột trong những bí quyết quan trọng nhất là hiểu rõ tâm lý khách hàng. Người mua xe hạng sang thường không chỉ mua một phương tiện di chuyển, mà họ mua cả đẳng cấp và phong cách sống.\n\nVề mức hoa hồng, anh Đức tiết lộ rằng với mỗi chiếc xe có giá từ vài tỷ đồng, nhân viên sales có thể nhận được mức hoa hồng từ 0.5% đến 1% giá trị xe, tương đương hàng chục đến hàng trăm triệu đồng.",
            "articleUrl": "https://dantri.com.vn/kinh-doanh/podcast-bi-mat-hoa-hong-ban-o-to-hang-sang-20230820091736120.htm",
            "audioUrl": null,
            "source": "Dân Trí",
            "publishedDate": "2023-08-20"
        },
        {
            "id": "2",
            "title": "Người kế nghiệp Biti's: 'Tự hỏi vì sao sinh ra trong gia đình này'",
            "thumbnailUrl": "https://picsum.photos/seed/article2/680/408",
            "contentSnippet": "Vưu Lệ Quyên, con gái cả của nhà sáng lập Biti's, từng phản kháng hoàn cảnh, cho rằng áp lực gia đình quá khắc nghiệt và rơi vào khủng hoảng hiện sinh.",
            "fullContent": "Vưu Lệ Quyên, con gái cả của nhà sáng lập Biti's, từng phản kháng hoàn cảnh, cho rằng áp lực gia đình quá khắc nghiệt và rơi vào khủng hoảng hiện sinh khi nhận ra mình đã sống quá lâu theo kỳ vọng của người khác.\n\nTrong tập podcast The First Step, cô chia sẻ về hành trình từ một người thừa kế miễn cưỡng đến việc tìm thấy đam mê thực sự với công việc kinh doanh gia đình.\n\nBiti's, thương hiệu giày dép Việt Nam với hơn 40 năm lịch sử, đã trở thành biểu tượng của ngành công nghiệp giày dép trong nước. Việc kế nghiệp một thương hiệu lớn như vậy đặt ra nhiều thách thức cho thế hệ tiếp theo.\n\nVưu Lệ Quyên chia sẻ về những đêm mất ngủ, những câu hỏi về bản thân và cuối cùng là cách cô tìm được câu trả lời cho riêng mình.",
            "articleUrl": "https://vnexpress.net/nguoi-ke-nghiep-biti-s-tu-hoi-vi-sao-sinh-ra-trong-gia-dinh-nay-5001583.html",
            "audioUrl": null,
            "source": "VnExpress",
            "publishedDate": "2025-01-05"
        },
        {
            "id": "3",
            "title": "Nhìn lại 72 giờ Mỹ gây chấn động thế giới tại Venezuela",
            "thumbnailUrl": "https://picsum.photos/seed/article3/680/408",
            "contentSnippet": "Một tổng thống bị bắt khi đang ngủ, 150 máy bay xuất kích từ 20 căn cứ - cú sốc địa chính trị lớn nhất ở Tây bán cầu trong 3 ngày qua.",
            "fullContent": "Một tổng thống bị bắt khi đang ngủ, 150 máy bay xuất kích từ 20 căn cứ - đây là cú sốc địa chính trị lớn nhất ở Tây bán cầu trong 3 ngày qua.\n\nChiến dịch quân sự của Mỹ tại Venezuela đã làm rung chuyển cả khu vực Mỹ Latinh. Với sự phối hợp chặt chẽ giữa các lực lượng đặc nhiệm, không quân và hải quân, Mỹ đã thực hiện một cuộc tấn công chớp nhoáng nhằm vào chính quyền Maduro.\n\nTheo các nguồn tin, chiến dịch kéo dài khoảng 72 giờ, trong đó giai đoạn quyết định chỉ diễn ra trong vòng 140 phút. Hệ thống phòng không của Venezuela bị vô hiệu hóa hoàn toàn trong những phút đầu tiên.\n\nPodcast VnExpress Hôm nay phân tích chi tiết diễn biến và hệ quả của sự kiện này đối với tình hình địa chính trị khu vực.",
            "articleUrl": "https://vnexpress.net/nhin-lai-72-gio-my-gay-chan-dong-the-gioi-tai-venezuela-5001707.html",
            "audioUrl": null,
            "source": "VnExpress",
            "publishedDate": "2025-01-05"
        },
        {
            "id": "4",
            "title": "Ông Trump nói về chiến dịch 140 phút bắt Tổng thống Maduro",
            "thumbnailUrl": "https://picsum.photos/seed/article4/680/408",
            "contentSnippet": "Tổng thống Trump họp báo về chiến dịch quân sự kéo dài 2 tiếng 20 phút, với sự tham gia của khoảng 150 máy bay từ 20 căn cứ.",
            "fullContent": "Tổng thống Donald Trump đã tổ chức họp báo để nói về chiến dịch quân sự tại Venezuela - một chiến dịch kéo dài khoảng 2 tiếng 20 phút.\n\nTheo ông Trump, lực lượng đặc nhiệm đã chuẩn bị các phương án dự phòng, bao gồm cả hành động tiêu diệt Tổng thống Maduro nếu không thể bắt giữ. Trump nhấn mạnh độ chính xác của chiến dịch và việc không có binh sĩ hay thiết bị quân sự Mỹ nào bị tổn thất.\n\nChiến dịch huy động khoảng 150 máy bay từ 20 căn cứ, bao gồm F-22, F-35 và các máy bay hỗ trợ khác. Hệ thống phòng không của Venezuela bị vô hiệu hóa và khu nhà ở của Maduro bị kiểm soát.\n\nTrump cũng đề cập đến kế hoạch quản lý Venezuela sau chiến dịch, phục hồi cơ sở hạ tầng dầu mỏ và chuẩn bị cho các giai đoạn tiếp theo nếu cần thiết.",
            "articleUrl": "https://vnexpress.net/ong-trump-noi-ve-chien-dich-140-phut-bat-tong-thong-maduro-5001655.html",
            "audioUrl": null,
            "source": "VnExpress",
            "publishedDate": "2025-01-04"
        },
        {
            "id": "5",
            "title": "Xu hướng công nghệ 2025: AI thay đổi mọi ngành nghề",
            "thumbnailUrl": "https://picsum.photos/seed/article5/680/408",
            "contentSnippet": "Trí tuệ nhân tạo đang định hình lại mọi ngành công nghiệp. Năm 2025 chứng kiến sự bùng nổ của AI trong mọi lĩnh vực từ y tế đến giáo dục.",
            "fullContent": "Năm 2025 được dự đoán là năm bùng nổ của trí tuệ nhân tạo (AI) với nhiều ứng dụng thực tế được triển khai rộng rãi trong các ngành công nghiệp.\n\nTheo báo cáo của McKinsey, AI có thể tạo ra giá trị kinh tế từ 2.6 đến 4.4 nghìn tỷ USD mỗi năm cho nền kinh tế toàn cầu. Các lĩnh vực được hưởng lợi nhiều nhất bao gồm chăm sóc sức khỏe, tài chính, bán lẻ và sản xuất.\n\nTại Việt Nam, nhiều doanh nghiệp đã bắt đầu ứng dụng AI vào hoạt động kinh doanh. Từ chatbot hỗ trợ khách hàng đến các hệ thống phân tích dữ liệu phức tạp, AI đang dần trở thành công cụ không thể thiếu.\n\nCác chuyên gia khuyến nghị doanh nghiệp nên bắt đầu từ những dự án nhỏ, tích lũy kinh nghiệm trước khi triển khai AI ở quy mô lớn hơn.",
            "articleUrl": "https://vnexpress.net/cong-nghe/ai-2025",
            "audioUrl": null,
            "source": "VnExpress",
            "publishedDate": "2025-01-03"
        },
        {
            "id": "6",
            "title": "Bất động sản 2025: Thị trường hồi phục sau giai đoạn khó khăn",
            "thumbnailUrl": "https://picsum.photos/seed/article6/680/408",
            "contentSnippet": "Thị trường bất động sản đang dần phục hồi sau giai đoạn khó khăn. Các chuyên gia dự báo xu hướng mới cho năm 2025.",
            "fullContent": "Sau một năm 2024 đầy biến động, thị trường bất động sản Việt Nam đang có những tín hiệu phục hồi tích cực. Các chuyên gia dự báo năm 2025 sẽ là năm của sự ổn định và phát triển bền vững hơn.\n\nPhân khúc nhà ở vừa túi tiền tiếp tục là điểm sáng của thị trường. Với nguồn cung hạn chế và nhu cầu thực cao, giá nhà tại các khu vực ngoại thành Hà Nội và TP.HCM vẫn duy trì ở mức ổn định.\n\nBất động sản công nghiệp và logistics cũng được đánh giá cao nhờ làn sóng đầu tư FDI và sự phát triển của thương mại điện tử.\n\nTuy nhiên, thị trường vẫn đối mặt với nhiều thách thức. Lãi suất tuy đã giảm nhưng còn cao so với giai đoạn trước. Các thủ tục pháp lý phức tạp cũng là rào cản lớn cho các dự án mới.",
            "articleUrl": "https://dantri.com.vn/bat-dong-san-2025",
            "audioUrl": null,
            "source": "Dân Trí",
            "publishedDate": "2025-01-02"
        }
    ]
    """.trimIndent()
}
