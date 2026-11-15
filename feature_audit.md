# BÁO CÁO KIỂM TOÁN TÍNH NĂNG NGƯỜI DÙNG (FEATURE AUDIT REPORT)
**Dự án:** Ứng dụng Nhắn tin & Gọi thoại Đa phương tiện Chatly (Android)  
**Thời điểm thực hiện:** 15/11/2026  
**Đơn vị thực hiện:** Product Management & QA Lead  
**Tài liệu tham chiếu:** Toàn bộ mã nguồn giao diện (UI/XML), ViewModels, Repositories, Firebase Cloud Services & ZegoCloud SDK.

---

## 1. BẢNG TỔNG QUAN TIẾN ĐỘ THEO TỪNG TÍNH NĂNG (Feature Status Dashboard)

| Nhóm tính năng | Tên tính năng cụ thể | Trạng thái hoạt động thực tế | Những gì đã chạy được | Những gì còn thiếu / Bị lỗi |
| :--- | :--- | :--- | :--- | :--- |
| **1. Tài khoản & Đăng nhập** | **Khởi động & Điều hướng (Splash)** | `[Hoàn thiện 100%]` | Tự động kiểm tra trạng thái đăng nhập Firebase Auth, tự điều hướng vào `MainActivity` nếu đã đăng nhập hoặc `LoginActivity` nếu chưa. | Không có. |
| | **Đăng nhập Email & Mật khẩu** | `[Hoàn thiện 100%]` | Xác thực với Firebase Auth, cập nhật trạng thái `online`, chuyển tiếp vào giao diện chính khi thành công, báo lỗi khi sai thông tin. | Không có. |
| | **Đăng ký Tài khoản mới** | `[Hoàn thiện 100%]` | Tạo user Firebase Auth, tạo document trong collection `users` trên Firestore với avatar mặc định, chuẩn hóa dữ liệu tìm kiếm. | Không có. |
| | **Đăng nhập bằng Google** | `[Mới có giao diện, chưa có chức năng]` | Nút "Tiếp tục với Google" hiển thị đẹp mắt trên màn hình Đăng nhập (`activity_login.xml`). | Bấm vào chỉ hiện `Toast: "Đăng nhập Google sắp ra mắt"`, chưa gọi Google Sign-In SDK / Credential Manager. |
| | **Quên mật khẩu** | `[Mới có giao diện, chưa có chức năng]` | Dòng chữ "Quên mật khẩu?" hiển thị tại màn hình Đăng nhập. | Bấm vào chỉ hiện `Toast: "Chức năng quên mật khẩu sắp ra mắt"`, chưa mở dialog nhập email hay gọi `sendPasswordResetEmail()`. |
| | **Đăng xuất** | `[Hoàn thiện 100%]` | Nút Đăng xuất trong tab Cá nhân cập nhật trạng thái `offline` lên Firestore, đăng xuất Firebase Auth và quay lại màn hình Login. | Không có. |
| **2. Hồ sơ cá nhân** | **Xem thông tin cá nhân** | `[Hoàn thiện 100%]` | Hiển thị chính xác ảnh đại diện, tên người dùng, email lấy từ Firestore qua LiveData. | Không có. |
| | **Đổi tên người dùng** | `[Hoàn thiện 100%]` | Nhập tên mới trong `EditProfileActivity`, bấm Lưu -> cập nhật thành công lên Firestore (`displayName` và `displayNameLower`). | Không có. |
| | **Đổi ảnh đại diện (Thư viện ảnh)** | `[Hoàn thiện 100%]` | Chọn ảnh từ thư viện, tải lên Firebase Storage (`/avatars/`), cập nhật URL avatar vào hồ sơ người dùng trên Firestore. | Không có. |
| | **Chụp ảnh đại diện mới / Xóa ảnh** | `[Mới có giao diện, chưa có chức năng]` | Menu đáy (Bottom Sheet) hiển thị 3 lựa chọn: "Chọn từ thư viện", "Chụp ảnh mới", "Xóa ảnh đại diện". | Chỉ có "Chọn từ thư viện" hoạt động; 2 mục "Chụp ảnh mới" và "Xóa ảnh" bấm vào không phản hồi (chưa gắn sự kiện). |
| | **Trạng thái Online / Offline / Last seen** | `[Hoàn thiện 100%]` | Tự động cập nhật `online = true` khi mở app (`onResume`) và `online = false` kèm thời gian `lastSeen` khi ẩn app (`onPause`). | Không có. |
| | **Đổi Email / Số điện thoại / Quyền riêng tư** | `[Mới có giao diện, chưa có chức năng]` | Các hàng mục "Đổi email", "Số điện thoại", "Quyền riêng tư" có sẵn giao diện trên màn hình Profile. | Bấm vào không có phản hồi (đang để hàm trống `TODO`). |
| **3. Bạn bè & Danh bạ** | **Tìm kiếm người dùng khác** | `[Hoàn thiện 100%]` | Màn hình `NewChatActivity` cho phép tìm kiếm theo Tên hoặc Email, có cơ chế debounce độ trễ 400ms, không phân biệt chữ hoa/thường. | Không có. |
| | **Bắt đầu cuộc trò chuyện từ tìm kiếm** | `[Hoàn thiện 100%]` | Bấm vào người dùng tìm thấy -> tự động kiểm tra xem đã có phòng chat chưa (nếu chưa thì tạo mới) và mở ngay màn hình chat. | Không có. |
| | **Danh sách bạn bè (Tab Danh bạ)** | `[Chạy một phần]` | Tab Danh bạ hiển thị được danh sách người dùng lấy từ Firestore kèm thanh cuộn chữ cái A-Z. | Đang lấy **toàn bộ người dùng** trong hệ thống (`getAllUsers()`) thay vì chỉ lọc những người đã là Bạn bè trong collection `friendships`. |
| | **Nút "Thêm bạn" trên thanh tiêu đề** | `[Mới có giao diện, chưa có chức năng]` | Icon dấu cộng `btnAddFriend` hiển thị ở góc trên màn hình Danh bạ. | Bấm vào không có phản hồi (chưa gán sự kiện mở màn hình tìm kiếm). |
| | **Xem & Xử lý lời mời kết bạn (Pending)** | `[Chạy một phần]` | Backend đã có đầy đủ logic gửi/nhận/chấp nhận/từ chối lời mời kết bạn trong `ContactRepository`. | Khung thông báo "Lời mời kết bạn" trên giao diện Danh bạ đang bị ẩn cứng (`View.GONE`), chưa có màn hình danh sách lời mời để bấm Duyệt. |
| **4. Chat 1-1 (Văn bản)** | **Danh sách cuộc trò chuyện (Hộp thư đến)** | `[Hoàn thiện 100%]` | Hiển thị danh sách chat sắp xếp theo tin mới nhất, cập nhật Realtime: avatar đối phương, tên, nội dung tin nhắn cuối, thời gian, huy hiệu chưa đọc. | Không có. |
| | **Gửi & Nhận tin nhắn văn bản** | `[Hoàn thiện 100%]` | Gửi tin nhắn tức thì, cập nhật Realtime hai chiều, hiển thị bong bóng chat chuẩn thẩm mỹ, tự động cuộn xuống cuối khi có tin mới. | Không có. |
| | **Trạng thái tin nhắn (Gửi / Nhận / Đã xem)** | `[Hoàn thiện 100%]` | Hiển thị biểu tượng 1 tích (đã gửi) / 2 tích (đã nhận) / 2 tích xanh (đã đọc). Tự động đánh dấu `read` khi đối phương mở phòng chat. | Không có. |
| | **Thu hồi tin nhắn (Recall)** | `[Hoàn thiện 100%]` | Nhấn giữ/click vào tin nhắn của mình -> chọn "Thu hồi" -> nội dung đổi thành "Tin nhắn đã được thu hồi" trên máy cả 2 người. | Không có. |
| | **Xóa tin nhắn phía tôi (Delete for me)** | `[Hoàn thiện 100%]` | Xóa tin nhắn khỏi màn hình của người bấm xóa, phía đối phương vẫn giữ nguyên tin nhắn. | Không có. |
| | **Cuộn lên để tải tin nhắn cũ (Pagination)** | `[Chạy một phần]` | Backend `ChatRepository` đã có hàm `loadMoreMessages()` hỗ trợ phân trang Cursor `DocumentSnapshot`. | Giao diện RecyclerView chưa bắt sự kiện cuộn kịch nóc để tự động gọi tải thêm tin nhắn cũ. |
| **5. Chat Nhóm (Group Chat)** | **Tạo nhóm chat mới** | `[Chưa làm gì]` | Backend đã có cấu trúc dữ liệu `Chat` (hỗ trợ `type = "group"`, `adminIds`, `groupName`) và hàm `createGroupChat()`. | Chưa có giao diện màn hình chọn bạn bè tạo nhóm, đặt tên nhóm, ảnh đại diện nhóm. |
| | **Quản lý thành viên nhóm** | `[Chưa làm gì]` | Backend đã chuẩn bị sẵn hàm thêm/xóa thành viên (`addParticipants`, `removeParticipant`). | Chưa có giao diện xem danh sách thành viên nhóm, đổi vai trò Admin, rời nhóm. |
| **6. Gửi Media / Voice / Vị trí** | **Menu chọn loại đính kèm (Nút `+`)** | `[Mới có giao diện, chưa có chức năng]` | Bấm nút `+` bên cạnh ô nhập tin nhắn mở ra Bottom Sheet đẹp mắt với 5 lựa chọn: Thư viện, Camera, File, Vị trí, Ghi âm. | Khi bấm vào bất kỳ lựa chọn nào trong 5 mục, app chỉ hiện `Toast: "Đã chọn: ..."` mà không kích hoạt thao tác thực tế nào. |
| | **Nút chụp ảnh nhanh (Camera) & Ghi âm nhanh** | `[Mới có giao diện, chưa có chức năng]` | 2 nút icon Camera và Mic xuất hiện sẵn ngay trên thanh nhập tin nhắn (`btnCamera`, `btnMic`). | Bấm hoặc giữ vào cả 2 nút này đều không có phản hồi (chưa được gán sự kiện lắng nghe). |
| | **Gửi ảnh & Video từ thư viện / camera** | `[Chạy một phần]` | Backend `sendMediaMessage()` và lớp chọn ảnh `MediaPickerHelper` đã hoàn chỉnh 100%. | Đang bị đứt đoạn do nút bấm trên giao diện chưa gọi tới Helper. Người dùng hiện chưa thể gửi ảnh/video từ app. |
| | **Gửi tệp tài liệu (PDF, Word, File)** | `[Chạy một phần]` | Backend hỗ trợ upload file lên Storage và lưu message `TYPE_FILE`. | Nút "Tệp tin" trong menu đính kèm chưa mở trình chọn file hệ thống (`ACTION_GET_CONTENT`). |
| | **Chia sẻ vị trí GPS hiện tại** | `[Chạy một phần]` | Helper `LocationHelper` đã lấy được tọa độ GPS và địa chỉ text; Backend hỗ trợ lưu message `TYPE_LOCATION`. | Nút "Vị trí" trong menu đính kèm chưa gọi `LocationHelper` để gửi tin nhắn. |
| | **Xem vị trí trên bản đồ** | `[Chạy một phần]` | Đã có màn hình `LocationViewerActivity` tích hợp Google Maps để xem vị trí và chỉ đường. | Khi người dùng bấm vào tin nhắn Vị trí trong phòng chat, code lại mở nhầm sang `MediaViewerActivity` dẫn tới báo lỗi không tải được ảnh. |
| | **Ghi âm & Gửi tin nhắn thoại (Voice note)** | `[Chạy một phần]` | `VoiceRecorderManager` đã viết xong logic ghi file âm thanh `.m4a` kèm đo biên độ sóng âm. | Nút Mic chưa kích hoạt bộ ghi âm và chưa gửi file lên Firebase. |
| | **Phát tin nhắn thoại trong phòng chat** | `[Mới có giao diện, chưa có chức năng]` | Bong bóng tin nhắn thoại hiển thị thanh sóng âm, thời lượng và nút Play tròn. | Bấm nút Play trên bong bóng chat không phát âm thanh (chưa gắn kết nối với `VoicePlayerManager`). |
| | **Màn hình xem ảnh / video toàn màn hình** | `[Hoàn thiện 100%]` | Màn hình `MediaViewerActivity` phóng to/thu nhỏ ảnh mượt mà, phát video và có nút chia sẻ/tải về. | Không có. |
| **7. Cuộc gọi Thoại & Video** | **Nút gọi Thoại & gọi Video trong phòng chat** | `[Mới có giao diện, chưa có chức năng]` | Hai icon Gọi thoại (`btnCallVoice`) và Gọi video (`btnCallVideo`) hiển thị rõ ràng trên thanh tiêu đề `ChatActivity`. | Bấm vào cả 2 nút đều **hoàn toàn không có phản hồi** (chưa gắn `setOnClickListener`). |
| | **Dịch vụ nhận cuộc gọi đến (ZegoCloud)** | `[Chạy một phần]` | Đã tích hợp thư viện ZegoUIKitPrebuiltCall và cấu hình AppID / AppSign trong code. | `ChatlyApplication` chưa gọi `initCallService()` sau khi người dùng đăng nhập, dẫn tới cuộc gọi không thể kết nối. |
| **8. Thông báo đẩy (Push Notification)** | **Tự động gửi thông báo khi có tin nhắn mới** | `[Chạy một phần]` | Cloud Function `onChatMessageCreated` đã viết xong để tự động kích hoạt gửi FCM tới người nhận. | Không có. |
| | **Nhận thông báo khi tắt app trên Android 13+** | `[Chạy một phần]` | Service `ChatlyFirebaseMessagingService` đã đăng ký trong Manifest và cấu hình Notification Channel. | App chưa có đoạn mã xin quyền `POST_NOTIFICATIONS` lúc mở app trên Android 13+, dẫn tới hệ thống tự chặn thông báo. |
| **9. Cài đặt & Tiện ích** | **Chế độ Sáng / Tối (Dark mode)** | `[Chưa làm gì]` | Chưa có tùy chọn trong giao diện Cài đặt. | App chưa hỗ trợ chuyển đổi giao diện Dark theme. |
| | **Đổi mật khẩu** | `[Chưa làm gì]` | Chưa có chức năng đổi mật khẩu cho tài khoản đang đăng nhập. | Cần bổ sung dialog/màn hình đổi mật khẩu trong tab Hồ sơ. |

---

## 2. DANH SÁCH CÁC TÍNH NĂNG ĐÃ HOÀN THIỆN (Có Thể Dùng Để Demo Ngay)

Đây là những tính năng đã chạy thông suốt từ giao diện người dùng (UI) xuống tới tầng cơ sở dữ liệu Firebase Firestore / Storage. Nhóm có thể tự tin sử dụng các tính năng này để ghi hình demo hoặc trình bày trước hội đồng:

### 1. Luồng Xác thực: Đăng ký & Đăng nhập Email/Mật khẩu
- **Thao tác người dùng:**
  1. Người dùng mở app, thấy màn hình Splash tự kiểm tra phiên.
  2. Tại màn hình Đăng ký, nhập Tên hiển thị, Email và Mật khẩu -> Bấm "Đăng ký".
  3. Hệ thống tạo tài khoản trên Firebase Auth, tự động gán avatar mặc định, lưu hồ sơ vào Firestore và tự động đăng nhập.
  4. Người dùng đăng xuất -> Nhập lại Email/Mật khẩu ở màn hình Đăng nhập -> Vào thẳng màn hình chính, hệ thống tự động chuyển trạng thái sang `Online`.

### 2. Xem và Chỉnh sửa Hồ sơ Cá nhân (Đổi tên & Đổi Avatar từ Thư viện)
- **Thao tác người dùng:**
  1. Chuyển sang tab "Cá nhân" (Profile), app hiển thị đầy đủ avatar, tên hiển thị và email.
  2. Bấm vào nút "Chỉnh sửa hồ sơ" -> Mở `EditProfileActivity`.
  3. Bấm vào icon máy ảnh trên avatar -> Chọn "Chọn từ thư viện" -> Trình chọn ảnh hệ thống mở ra.
  4. Chọn một tấm ảnh -> Ảnh được tải lên Firebase Storage và tự động cập nhật URL mới vào Firestore.
  5. Sửa tên hiển thị -> Bấm "Lưu" -> Thông báo cập nhật thành công, màn hình profile tự làm mới tên mới ngay lập tức.

### 3. Tìm kiếm Người dùng & Mở phòng Chat 1-1
- **Thao tác người dùng:**
  1. Tại màn hình Tin nhắn, bấm vào nút soạn tin mới (FAB / icon bút viết).
  2. Màn hình `NewChatActivity` xuất hiện với thanh tìm kiếm.
  3. Nhập từ khóa (tên hoặc email, không cần quan tâm chữ hoa hay chữ thường) -> Kết quả tìm kiếm hiển thị danh sách người dùng tương ứng theo thời gian thực (đã có cơ chế debounce mượt mà).
  4. Bấm vào một người dùng trong danh sách -> Hệ thống tự kiểm tra: nếu đã từng nhắn tin thì mở lại phòng chat cũ, nếu chưa thì tạo phòng chat mới và chuyển người dùng vào `ChatActivity`.

### 4. Nhắn tin Văn bản Thời gian Thực (Realtime 1-1 Messaging)
- **Thao tác người dùng:**
  1. Trong phòng chat 1-1, người dùng gõ nội dung vào ô nhập văn bản và bấm nút "Gửi" (icon máy bay giấy).
  2. Tin nhắn xuất hiện ngay lập tức trên màn hình của người gửi với icon 1 dấu tích (Đã gửi).
  3. Phía người nhận thấy tin nhắn mới nhảy vào màn hình tức thì không cần tải lại trang.
  4. Khi người nhận mở phòng chat, trạng thái tin nhắn bên máy người gửi tự động nhảy sang 2 dấu tích xanh (Đã xem).

### 5. Thu hồi & Xóa tin nhắn
- **Thao tác người dùng:**
  1. Nhấn giữ vào một tin nhắn văn bản do chính mình gửi.
  2. Chọn "Thu hồi tin nhắn" -> Nội dung tin nhắn trên cả hai máy lập tức chuyển thành *"Tin nhắn đã được thu hồi"*, ngăn đối phương đọc tiếp nội dung cũ.
  3. Nếu chọn "Xóa ở phía tôi" -> Tin nhắn biến mất khỏi danh sách chat của người thao tác nhưng máy đối phương vẫn còn nguyên.

### 6. Xem Ảnh & Video Toàn màn hình (Media Viewer)
- **Thao tác người dùng:**
  - Khi xem một đường dẫn ảnh/video hợp lệ trong app, mở `MediaViewerActivity` cho phép xem toàn màn hình, chạm để ẩn/hiện thanh công cụ, phóng to thu nhỏ (zoom đa điểm) và nút chia sẻ tệp.

---

## 3. DANH SÁCH CÁC TÍNH NĂNG CHƯA HOÀN THIỆN / CÒN LỖI (Trọng Tâm Cần Xử Lý)

Dưới đây là chi tiết các tính năng đang làm dở, gặp lỗi logic hoặc có giao diện nhưng chưa được gắn chức năng:

---

### 🔴 Tính năng 1: Gửi Tệp đính kèm trong phòng Chat (Ảnh, Camera, File, Vị trí, Ghi âm)
- **Hiện trạng khi người dùng thao tác trên app:**  
  Người dùng đang chat, bấm nút dấu cộng `+` bên cạnh ô nhập tin nhắn -> Bảng chọn (Bottom Sheet) hiện lên với 5 biểu tượng rất đẹp: Thư viện, Máy ảnh, Tài liệu, Vị trí, Ghi âm. Tuy nhiên khi bấm vào **bất kỳ mục nào**, app chỉ hiện lên một thông báo nổi (Toast): *"Đã chọn: GALLERY"* hoặc *"Đã chọn: LOCATION"* rồi biến mất, hoàn toàn không mở bộ chọn ảnh hay gửi được bất kỳ nội dung nào.
- **Phần việc cụ thể cần làm nốt:**  
  Toàn bộ code xử lý tải file lên Storage và hàm gửi tin nhắn media (`sendMediaMessage`) đã được viết sẵn trong Backend, chỉ thiếu việc nối sự kiện:
  1. Khi bấm **Thư viện / Máy ảnh**: Gọi `MediaPickerHelper` để mở bộ chọn ảnh/video hoặc máy ảnh hệ thống, sau khi nhận kết quả `Uri` thì gọi `chatViewModel.sendMediaMessage()`.
  2. Khi bấm **Tài liệu**: Khởi chạy `Intent.ACTION_GET_CONTENT` cho phép chọn file PDF/Word/ZIP, lấy tên file và dung lượng rồi gửi qua `sendMediaMessage()`.
  3. Khi bấm **Vị trí**: Gọi `LocationHelper.getCurrentLocation()`, lấy tọa độ Lat/Lng và địa chỉ rồi gửi tin nhắn dạng `TYPE_LOCATION`.
  4. Khi bấm **Ghi âm**: Mở thanh công cụ ghi âm giọng nói.  
  *(Vị trí code cần sửa: `ChatActivity.java` - hàm `onAttachmentOption()` và phương thức khởi tạo launcher)*.

---

### 🔴 Tính năng 2: Hai nút bấm Nhanh trên thanh nhập chat (Camera & Ghi âm) bị liệt
- **Hiện trạng khi người dùng thao tác trên app:**  
  Trên thanh gõ tin nhắn luôn có sẵn 2 icon: Máy ảnh (`btnCamera`) và Micro (`btnMic`). Người dùng bấm vào nút Máy ảnh hoặc bấm/giữ vào nút Micro thì hoàn toàn không có bất kỳ hiện tượng gì xảy ra.
- **Phần việc cụ thể cần làm nốt:**  
  1. Gán sự kiện click cho `btnCamera`: Mở camera chụp ảnh nhanh tương tự tùy chọn Camera trong menu đính kèm.
  2. Gán sự kiện chạm/giữ (`OnTouchListener`) cho `btnMic`: Khi nhấn giữ thì bắt đầu ghi âm qua `VoiceRecorderManager` đồng thời hiển thị giao diện thời gian ghi âm (`layout_voice_recording`); khi buông tay thì dừng ghi âm và tự động gửi file âm thanh; nếu vuốt sang trái thì hủy bỏ bản ghi.  
  *(Vị trí code cần sửa: `ChatActivity.java` - hàm `initViews()`)*.

---

### 🔴 Tính năng 3: Bấm vào tin nhắn Vị trí bị báo lỗi đen màn hình / không xem được bản đồ
- **Hiện trạng khi người dùng thao tác trên app:**  
  Nếu trong cuộc trò chuyện có một tin nhắn Vị trí GPS (được gửi từ trước), người dùng bấm vào bong bóng tin nhắn để xem bản đồ thì app mở màn hình xem ảnh (`MediaViewerActivity`) thay vì mở bản đồ, dẫn tới thông báo lỗi không tải được hình ảnh.
- **Phần việc cụ thể cần làm nốt:**  
  Trong sự kiện click vào tin nhắn đa phương tiện (`mediaClickListener`), cần kiểm tra loại tin nhắn: nếu là `Message.TYPE_LOCATION` thì chuyển hướng mở màn hình `LocationViewerActivity` kèm các thông số tọa độ `EXTRA_LATITUDE`, `EXTRA_LONGITUDE`, `EXTRA_TITLE`.  
  *(Vị trí code cần sửa: `ChatActivity.java` - hàm `setupRecyclerView()`)*.

---

### 🔴 Tính năng 4: Nút Gọi Thoại và Gọi Video trong phòng chat bị liệt
- **Hiện trạng khi người dùng thao tác trên app:**  
  Trên thanh tiêu đề (header) của màn hình chat luôn có 2 nút gọi: icon điện thoại (`btnCallVoice`) và icon máy quay (`btnCallVideo`). Người dùng bấm vào 2 nút này đều không có phản hồi.
- **Phần việc cụ thể cần làm nốt:**  
  1. Trong `ChatlyApplication.java`, gọi `initCallService(currentUserId, displayName)` sau khi người dùng đăng nhập để khởi động dịch vụ ZegoCloud lắng nghe cuộc gọi.
  2. Trong `ChatActivity.java`, gán `setOnClickListener` cho `btnCallVoice` và `btnCallVideo` để gọi hàm bắt đầu cuộc gọi 1-1 qua Zego SDK tới `receiverId`.  
  *(Vị trí code cần sửa: `ChatlyApplication.java` và `ChatActivity.java`)*.

---

### 🔴 Tính năng 5: Bong bóng Tin nhắn Thoại không phát được âm thanh
- **Hiện trạng khi người dùng thao tác trên app:**  
  Bong bóng tin nhắn thoại hiển thị nút Play tròn màu xanh cùng sóng âm. Người dùng bấm vào nút Play thì icon không chuyển sang Pause và không có tiếng phát ra loa.
- **Phần việc cụ thể cần làm nốt:**  
  Trong `MessageAdapter.java` tại 2 lớp hiển thị tin nhắn thoại (`SentVoiceViewHolder` và `ReceivedVoiceViewHolder`), gán sự kiện click cho nút Play để gọi `VoicePlayerManager.getInstance().play()` với đường dẫn URL file âm thanh, đồng thời cập nhật thanh tiến trình chạy theo thời gian thực.  
  *(Vị trí code cần sửa: `MessageAdapter.java`)*.

---

### 🔴 Tính năng 6: Màn hình Danh bạ hiển thị sai đối tượng & Nút "Thêm bạn" không hoạt động
- **Hiện trạng khi người dùng thao tác trên app:**  
  1. Khi người dùng bấm sang tab "Danh bạ", danh sách hiện lên toàn bộ tài khoản có trong hệ thống thay vì chỉ hiển thị những người đã kết bạn.
  2. Nút dấu cộng "Thêm bạn" (`btnAddFriend`) ở góc trên màn hình bấm vào không mở màn hình tìm kiếm bạn bè.
  3. Khung thông báo "Lời mời kết bạn" hoàn toàn không hiển thị dù có người gửi lời mời.
- **Phần việc cụ thể cần làm nốt:**  
  1. Sửa `ContactsFragment.java`: Gán sự kiện click cho `btnAddFriend` để mở màn hình tìm kiếm `NewChatActivity`.
  2. Thay đổi truy vấn dữ liệu từ `getAllUsers()` sang lắng nghe danh sách bạn bè đã chấp nhận (`status == "accepted"`) từ collection `friendships`.
  3. Mở hiển thị khung lời mời kết bạn khi có lời mời đang chờ (`pending`) và tạo dialog/màn hình danh sách lời mời kèm 2 nút "Đồng ý" / "Từ chối".  
  *(Vị trí code cần sửa: `ContactsFragment.java`, `ContactsViewModel.java`)*.

---

### 🔴 Tính năng 7: Chụp ảnh trực tiếp làm avatar & Xóa avatar chưa hoạt động
- **Hiện trạng khi người dùng thao tác trên app:**  
  Tại màn hình Chỉnh sửa hồ sơ, bấm vào avatar -> Chọn "Chụp ảnh mới" hoặc "Xóa ảnh đại diện" -> Menu đóng lại nhưng không có máy ảnh nào mở lên và ảnh cũng không bị xóa.
- **Phần việc cụ thể cần làm nốt:**  
  1. Gán sự kiện cho `row_take_photo`: Gọi launcher mở Camera chụp ảnh và lấy file kết quả upload lên Storage.
  2. Gán sự kiện cho `row_remove_photo`: Gọi `profileViewModel.updateAvatar(null)` để xóa URL ảnh trên Firestore và đưa avatar về ảnh mặc định.  
  *(Vị trí code cần sửa: `EditProfileActivity.java`)*.

---

### 🔴 Tính năng 8: Nút Đăng nhập Google & Quên mật khẩu chỉ là nút giả
- **Hiện trạng khi người dùng thao tác trên app:**  
  Ở màn hình đăng nhập, bấm vào "Tiếp tục với Google" hoặc bấm vào "Quên mật khẩu?" đều chỉ hiện thông báo Toast *"Sắp ra mắt"*.
- **Phần việc cụ thể cần làm nốt:**  
  1. Đối với "Quên mật khẩu": Tạo một `AlertDialog` đơn giản có ô nhập email, khi bấm Gửi thì gọi hàm `FirebaseAuth.getInstance().sendPasswordResetEmail(email)`.
  2. Đối với "Đăng nhập Google": Tích hợp Google Credential Manager API hoặc tạm thời ẩn nút này đi nếu chưa cấu hình SHA-1 fingerprint trên Firebase Console để tránh gây hiểu nhầm cho người chấm đồ án.  
  *(Vị trí code cần sửa: `LoginActivity.java`)*.

---

### 🔴 Tính năng 9: Không nhận được Thông báo đẩy trên các máy Android đời mới (Android 13+)
- **Hiện trạng khi người dùng thao tác trên app:**  
  Khi cài đặt app trên điện thoại chạy Android 13 trở lên, app không hề hỏi xin quyền hiển thị thông báo. Khi có tin nhắn đến lúc tắt app, điện thoại hoàn toàn im lặng.
- **Phần việc cụ thể cần làm nốt:**  
  Thêm đoạn code kiểm tra và xin quyền runtime `android.permission.POST_NOTIFICATIONS` khi người dùng vừa đăng nhập thành công vào `MainActivity`.  
  *(Vị trí code cần sửa: `MainActivity.java`)*.

---

## 4. CÁC TÍNH NĂNG QUAN TRỌNG CỦA MỘT APP CHAT NHƯNG CHƯA HỀ CÓ TRONG DỰ ÁN

Đây là các tính năng tiêu chuẩn (Industry-Standard Features) của các ứng dụng OTT như Zalo, Telegram, Messenger mà dự án hiện tại **hoàn toàn chưa có cả giao diện lẫn mã nguồn backend**:

1. **Chỉ báo "Đang soạn tin nhắn..." (Typing Indicator):**  
   Chưa có cơ chế ghi nhận và hiển thị dấu ba chấm nhấp nháy khi người bên kia đang gõ phím.
2. **Tìm kiếm tin nhắn trong cuộc trò chuyện (In-chat Message Search):**  
   Chưa có ô tìm kiếm từ khóa bên trong một phòng chat cụ thể để nhảy đến tin nhắn cũ.
3. **Thả cảm xúc tin nhắn (Message Reactions):**  
   Chưa hỗ trợ thả biểu tượng cảm xúc (Thả tim, Like, Haha, Buồn, Phẫn nộ) trực tiếp trên từng bong bóng tin nhắn.
4. **Trả lời tin nhắn cụ thể (Reply / Quote Message):**  
   Chưa có tính năng vuốt sang phải hoặc nhấn giữ để trích dẫn (quote) một tin nhắn trước đó khi trả lời.
5. **Ghim tin nhắn & Ghim cuộc trò chuyện (Pin Message / Pin Chat):**  
   Chưa có tính năng ghim tin nhắn quan trọng lên đỉnh phòng chat hoặc ghim hội thoại lên đầu danh sách tin nhắn.
6. **Chặn người dùng & Báo cáo (Block / Report User):**  
   Chưa có chức năng chặn người dùng làm phiền để không nhận tin nhắn hoặc cuộc gọi từ người đó.
7. **Tắt thông báo cuộc trò chuyện (Mute Notifications):**  
   Chưa có nút gạt tắt chuông/thông báo cho từng phòng chat riêng biệt.
8. **Chế độ giao diện Tối (Dark Theme):**  
   Toàn bộ app hiện chỉ chạy trên nền sáng (Light mode), chưa có theme ban đêm.

---

## 5. THỨ TỰ ƯU TIÊN HOÀN THIỆN CHỨC NĂNG CHO NHÓM (Roadmap to Submission)

Để đạt điểm số tối đa trong buổi báo cáo đồ án với thời gian tối ưu nhất, nhóm nên triển khai các đầu việc còn thiếu theo **3 đợt ưu tiên (Sprints)** như sau:

```
┌────────────────────────────────────────────────────────────────────────┐
│ ĐỢT 1: SỬA LỖI NÚT BẤM BỊ LIỆT & NỐI TÍNH NĂNG ĐÍNH KÈM (P0 - 1 Ngày)  │
├────────────────────────────────────────────────────────────────────────┤
│ 1. Nối 5 lựa chọn gửi file đính kèm trong ChatActivity (Ảnh, File, GPS)│
│ 2. Sửa lỗi click tin nhắn Vị trí mở nhầm MediaViewerActivity           │
│ 3. Nối nút Play trên bong bóng tin nhắn thoại với VoicePlayerManager   │
│ 4. Thêm quyền POST_NOTIFICATIONS trên Android 13+ trong MainActivity   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│ ĐỢT 2: KÍCH HOẠT CUỘC GỌI & HOÀN THIỆN DANH BẠ (P1 - 1 đến 2 Ngày)     │
├────────────────────────────────────────────────────────────────────────┤
│ 1. Khởi động Zego Call Service và nối 2 nút Gọi Thoại / Video          │
│ 2. Nối nút dấu cộng "Thêm bạn" mở NewChatActivity                      │
│ 3. Lọc đúng danh sách bạn bè đã kết bạn và hiển thị lời mời kết bạn    │
│ 4. Bổ sung gửi email Quên mật khẩu qua Firebase Auth                   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│ ĐỢT 3: TÍNH NĂNG NÂNG CAO NẾU CÒN THỜI GIAN (P2 - Tùy chọn cộng điểm)  │
├────────────────────────────────────────────────────────────────────────┤
│ 1. Xây dựng giao diện Tạo nhóm chat mới (CreateGroupActivity)          │
│ 2. Bổ sung trạng thái Đang gõ tin nhắn (Typing Indicator)              │
│ 3. Bổ sung tính năng Quét mã QR kết bạn cá nhân                        │
└────────────────────────────────────────────────────────────────────────┘
```

---
*Báo cáo được tổng hợp tự động từ việc rà soát 100% các file mã nguồn của dự án Chatly.*
