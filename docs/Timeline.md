# **Kế hoạch Phát triển Dự án: Ứng dụng giao tiếp trực tuyến đa phương tiện cho Android**

Tài liệu này chi tiết hóa lộ trình phát triển và phân chia trách nhiệm cho dự án ứng dụng Android sử dụng ngôn ngữ Java, tuân thủ mô hình Incremental Model nhằm đảm bảo tính ổn định và khả năng mở rộng của sản phẩm.

# **1\. Thông tin chung**

* **Tên đề tài:** Ứng dụng giao tiếp trực tuyến đa phương tiện cho Android.  
* **Ngôn ngữ lập trình:** Java (Android SDK).  
* **Nền tảng dịch vụ:** Firebase (BaaS \- Authentication, Firestore, Storage, Cloud Messaging).  
* **Mô hình phát triển:** Incremental Model (Phát triển lũy tiến qua các phân đoạn).

# **2\. Hệ thống tính năng chi tiết**

* **Tính năng cốt lõi (Core):**  
  * Xác thực người dùng: Đăng ký, đăng nhập, đăng xuất, quên mật khẩu.  
  * Quản lý tài khoản: Cập nhật hồ sơ (Avatar, tên hiển thị).  
  * Danh bạ: Tìm kiếm danh bạ, kết bạn.  
  * Chat cơ bản: Nhắn tin văn bản 1-1, Trạng thái tin nhắn (đã xem, typing...), gửi Sticker \- emoji.  
* **Tính năng đa phương tiện (Multimedia):**  
  * Media Sharing: Gửi media (hình ảnh, video, tệp tài liệu).  
  * Voice Messaging: Voice chat (ghi âm và gửi tin nhắn thoại).  
  * Vị trí: Chia sẻ vị trí hiện tại.  
* **Tính năng mở rộng (Advanced):**  
  * Group Chat: Trò chuyện nhóm.  
  * Call System: Audio/video call (sử dụng SDK).  
  * Bảo mật: Bảo mật tin nhắn/hộp thoại (Khóa hộp thoại bằng mật khẩu hoặc mã hóa cơ bản).  
  * Thông báo: Thông báo đẩy (Push Notifications qua FCM).

# **3\. Phân bổ nhân sự và Trách nhiệm**

| Thành viên | Vai trò chủ chốt | Trách nhiệm chi tiết |
| :---- | :---- | :---- |
| Person | **Backend & Data Lead** | Thiết lập cấu trúc Firebase, thiết kế sơ đồ dữ liệu (Firestore), viết logic Auth, xây dựng DAO/Repository pattern. |
| Person | **UI/UX & Chat Lead** | Thiết kế Layout (XML), quản lý Activity/Fragment, xử lý RecyclerView cho tin nhắn, tối ưu hóa luồng trải nghiệm người dùng. |
| Person | **Multimedia Lead** | Quản lý Permission (Runtime), tích hợp CameraX/MediaRecorder APIs, xử lý tải lên/tải xuống tệp (Firebase Storage), tích hợp Third-party Call SDK. |

# **4\. Lộ trình thực hiện (8 Tuần)**

| Giai đoạn | Thời gian | Mục tiêu trọng tâm | Nội dung công việc |
| :---- | :---- | :---- | :---- |
| **Khởi động** | Tuần 1 | Kiến trúc & Setup | Cấu hình dự án, thiết lập Firebase, thống nhất cấu trúc thư mục và Git Flow. |
| **Increment 1** | Tuần 2 \- 3 | Giao tiếp cơ bản | Hoàn thiện Module Auth, quản lý người dùng và chức năng chat văn bản thời gian thực. |
| **Increment 2** | Tuần 4 \- 5 | Đa phương tiện | Tích hợp gửi ảnh, tệp tin và tin nhắn thoại. Xử lý quyền truy cập bộ nhớ và phần cứng. |
| **Increment 3** | Tuần 6 | Tính năng nâng cao | Xây dựng logic trò chuyện nhóm và tích hợp SDK cho cuộc gọi trực tuyến. |
| **Hoàn thiện** | Tuần 7 \- 8 | Tích hợp & QC | Triển khai Firebase Cloud Messaging (FCM), sửa lỗi, kiểm thử trên thiết bị vật lý và đóng gói báo cáo. |

# **5\. Nguyên tắc kỹ thuật và Quy trình phối hợp**

* **IDE:** Android Studio.  
* **Quản lý mã nguồn:** Sử dụng Git. Mỗi thành viên làm việc trên nhánh `feature/[tên-tính-năng]` và thực hiện Pull Request vào nhánh `main` sau khi đã tự kiểm tra (Self-test) → Bắt buộc phải có reviewer cho PR (không tự review).  
* **Nguyên tắc Code:**  
  * Tuân thủ quy chuẩn đặt tên Java (CamelCase).  
  * Comment đầy đủ cho các hàm xử lý logic phức tạp.  
  * Tuyệt đối không sử dụng các ngôn ngữ khác ngoài Java trong logic xử lý.  
* **Phối hợp:** Kiểm tra tiến độ và giải quyết xung đột mã nguồn (Conflict) vào cuối mỗi tuần làm việc.

Duyệt bởi: Person  
Ngày phê duyệt: Date