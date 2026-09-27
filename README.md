# 📱 Chatly - Multimedia Communication App for Android

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Platform" />
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java Language" />
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase BaaS" />
  <img src="https://img.shields.io/badge/Architecture-MVVM-0052CC?style=for-the-badge" alt="MVVM Architecture" />
  <img src="https://img.shields.io/badge/Course-NT118-red?style=for-the-badge" alt="Course NT118" />
</p>

---

## 1. Giới thiệu dự án

**Chatly** là ứng dụng giao tiếp trực tuyến đa phương tiện (Multimedia Real-time Messaging & Calling) dành cho hệ điều hành Android, được phát triển bởi **Nhóm 12** trong khuôn khổ môn học **Phát triển ứng dụng trên thiết bị di động (NT118)**.

Ứng dụng được viết hoàn toàn bằng ngôn ngữ **Java**, áp dụng mô hình kiến trúc **MVVM (Model-View-ViewModel)** cùng nền tảng **Firebase BaaS**, mang đến trải nghiệm nhắn tin thời gian thực mượt mà, chia sẻ đa phương tiện nhanh chóng và các cuộc gọi thoại/video chất lượng cao.

---

## 2. Hệ thống tính năng chính

| Phân hệ | Tính năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **Cốt lõi (Core)** | Xác thực & Hồ sơ | Đăng ký, đăng nhập, quên mật khẩu, cập nhật avatar và thông tin cá nhân. |
| | Danh bạ & Kết bạn | Tìm kiếm người dùng qua Email/SĐT, gửi/nhận lời mời kết bạn, danh sách bạn bè. |
| | Chat 1-1 Thời gian thực | Nhắn tin văn bản tức thì, hiển thị trạng thái đã xem (seen), typing indicator, sticker & emoji. |
| **Đa phương tiện (Multimedia)** | Chia sẻ Media | Gửi và nhận hình ảnh (CameraX/Gallery), video, tài liệu đính kèm (PDF, DOCX, ZIP). |
| | Tin nhắn thoại (Voice) | Ghi âm tin nhắn thoại chất lượng cao (Hold to record, Waveform player). |
| | Chia sẻ vị trí | Chia sẻ vị trí hiện tại tức thì qua Google Maps / Location API. |
| **Nâng cao (Advanced)** | Trò chuyện nhóm (Group Chat) | Tạo nhóm, phân quyền Admin, thêm/xóa thành viên, gửi tin nhắn và media nhóm. |
| | Gọi thoại & Video Call | Cuộc gọi Audio/Video thời gian thực 1-1 tích hợp Call SDK. |
| | Bảo mật & Riêng tư | Khóa hộp thoại/ứng dụng bằng mã PIN hoặc vân tay (BiometricPrompt). |
| | Thông báo đẩy (FCM) | Nhận thông báo tin nhắn và cuộc gọi đến khi ứng dụng chạy ngầm hoặc đã đóng. |

---

## 3. Kiến trúc & Công nghệ sử dụng

* **Ngôn ngữ:** Java (JDK 17+)
* **Nền tảng mục tiêu:** Android SDK (Min SDK: 24 - Android 7.0 / Target SDK: 34 - Android 14)
* **Kiến trúc ứng dụng:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Nền tảng dịch vụ (Backend as a Service):**
  * **Firebase Authentication:** Quản lý xác thực người dùng.
  * **Cloud Firestore:** Cơ sở dữ liệu NoSQL thời gian thực lưu trữ tin nhắn, người dùng, nhóm.
  * **Firebase Storage:** Lưu trữ tệp media (hình ảnh, video, âm thanh, tài liệu).
  * **Firebase Cloud Messaging (FCM):** Dịch vụ thông báo đẩy (Push Notifications).
* **Thư viện & SDK bên thứ ba:**
  * **Image Loading:** Glide / Picasso
  * **Media Handling:** Android CameraX, AndroidX Media3 / ExoPlayer
  * **Call RTC Engine:** ZegoCloud RTC / Agora RTC SDK
  * **UI Components:** Material Design 3, CircleImageView, Lottie Animations

---

## 4. Cấu trúc thư mục mã nguồn

```
NT118_Group12_Chatly/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/group12/chatly/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/          # Data Models (User, Message, Chat, Group...)
│   │   │   │   │   ├── remote/         # Firebase Service Wrappers
│   │   │   │   │   └── repository/     # Repositories (Auth, Chat, User...)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/           # Login, Register, Forgot Password
│   │   │   │   │   ├── main/           # MainActivity & Navigation Tabs
│   │   │   │   │   ├── chat/           # 1-1 Chat, Group Chat, Adapters
│   │   │   │   │   ├── contact/        # Contacts, Search Users, Friend Requests
│   │   │   │   │   ├── media/          # Media Viewer, Camera, Audio Player
│   │   │   │   │   ├── call/           # Audio/Video Call Activities
│   │   │   │   │   └── profile/        # User Profile, Settings, Security
│   │   │   │   ├── viewmodel/          # MVVM ViewModels
│   │   │   │   ├── service/            # FCM Messaging Service, Call Service
│   │   │   │   ├── utils/              # Helpers (Constants, Perms, Crypto, Date)
│   │   │   │   └── ChatlyApp.java      # Application Class
│   │   │   ├── res/                    # Layouts, Drawables, Values, Colors
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit Tests
│   ├── google-services.json            # Cấu hình Firebase (Private)
│   └── build.gradle
├── docs/
│   ├── Timeline.md                     # Lộ trình 8 tuần & Phân công trách nhiệm
│   └── Project_Plan.md                 # Kế hoạch chi tiết, Kiến trúc & Data Model
├── README.md
└── build.gradle
```

---

## 5. Hướng dẫn cài đặt và chạy ứng dụng

### **Yêu cầu môi trường**
* **Android Studio:** Hedgehog (2023.1.1) hoặc mới hơn
* **JDK:** Java Development Kit 17
* **Thiết bị:** Thiết bị thật chạy Android 7.0 (API 24) trở lên hoặc Android Emulator

### **Các bước thiết lập**
1. **Clone mã nguồn từ GitHub:**
   ```bash
   git clone https://github.com/TranTran365/NT118_Group12_Chatly.git
   cd NT118_Group12_Chatly
   ```
2. **Cấu hình Firebase:**
   * Truy cập [Firebase Console](https://console.firebase.google.com/) và tạo project mới.
   * Thêm ứng dụng Android với package name: `com.group12.chatly`.
   * Tải tệp `google-services.json` và đặt vào thư mục `/app/`.
   * Bật các dịch vụ: **Authentication** (Email/Password), **Cloud Firestore**, **Storage**, và **Cloud Messaging**.
3. **Mở dự án:**
   * Mở **Android Studio** -> chọn `Open Project` -> trỏ tới thư mục `NT118_Group12_Chatly`.
   * Chờ Gradle đồng bộ (Sync Project with Gradle Files).
4. **Build & Chạy ứng dụng:**
   * Chọn thiết bị máy ảo (Emulator) hoặc cắm thiết bị thật (bật USB Debugging).
   * Nhấn nút **Run (Shift + F10)** để build và cài đặt ứng dụng.

---

## 6. Lộ trình phát triển (8 Tuần - Incremental Model)

| Giai đoạn | Thời gian | Trọng tâm công việc | Tài liệu chi tiết |
| :--- | :--- | :--- | :---: |
| **Khởi động** | Tuần 1 | Khởi tạo dự án, thiết lập Firebase, cấu trúc MVVM và quy chuẩn Git Flow. | [Xem Plan](docs/Project_Plan.md#tuần-1-khởi-động---kiến-trúc--thiết-lập-nền-tảng) |
| **Increment 1** | Tuần 2 - 3 | Xác thực (Auth), quản lý hồ sơ, danh bạ và nhắn tin văn bản 1-1 thời gian thực. | [Xem Plan](docs/Project_Plan.md#tuần-2--3-increment-1---xác-thực-người-dùng--giao-tiếp-cơ-bản) |
| **Increment 2** | Tuần 4 - 5 | Đa phương tiện: Gửi ảnh/video/tệp, ghi âm tin nhắn thoại và chia sẻ vị trí. | [Xem Plan](docs/Project_Plan.md#tuần-4--5-increment-2---đa-phương-tiện-multimedia) |
| **Increment 3** | Tuần 6 | Tính năng nâng cao: Trò chuyện nhóm (Group Chat) và Cuộc gọi Audio/Video. | [Xem Plan](docs/Project_Plan.md#tuần-6-increment-3---tính-năng-nâng-cao-group-chat--call-system) |
| **Hoàn thiện** | Tuần 7 - 8 | Thông báo đẩy FCM, khóa bảo mật hội thoại, kiểm thử toàn diện và đóng gói APK. | [Xem Plan](docs/Project_Plan.md#tuần-7--8-hoàn-thiện-tích-hợp--qc-testing--deployment) |

> Tham khảo tài liệu chi tiết: [`docs/Timeline.md`](docs/Timeline.md) và [`docs/Project_Plan.md`](docs/Project_Plan.md).

---

## 7. Phân công trách nhiệm (Nhóm 12)

| Thành viên | Vai trò | Trách nhiệm chính |
| :--- | :--- | :--- |
| **Thành viên 1** | **Backend & Data Lead** | Cấu hình Firebase (Auth, Firestore, Storage), thiết kế Data Schema, triển khai Repositories, xử lý logic Realtime & Cloud Messaging. |
| **Thành viên 2** | **UI/UX & Chat Lead** | Thiết kế Layouts XML, quản lý Activities/Fragments, xử lý RecyclerView chat, tối ưu trải nghiệm người dùng & tính năng bảo mật. |
| **Thành viên 3** | **Multimedia Lead** | Xử lý Runtime Permissions, tích hợp CameraX, MediaRecorder/Player, FusedLocation, tích hợp Third-party Audio/Video Call SDK. |

---

## 8. Quy chuẩn đóng góp mã nguồn (Git Workflow)

* **Nhánh chính:**
  * `main`: Nhánh ổn định, chứa các phiên bản phát hành chính thức.
  * `develop`: Nhánh tích hợp các tính năng trước khi release.
  * `feature/[tên-tính-năng]`: Nhánh phát triển của từng thành viên (vd: `feature/auth-login`, `feature/voice-message`).
* **Quy tắc làm việc:**
  1. Luôn tạo nhánh mới từ `develop` để làm việc.
  2. Không commit trực tiếp vào `main` và `develop`.
  3. Mở **Pull Request (PR)** vào `develop` sau khi đã tự kiểm tra (Self-test) kỹ lưỡng.
  4. **Bắt buộc có ít nhất 1 thành viên review và phê duyệt (Approve)** mới được merge PR.
  5. Tuân thủ định dạng commit message: `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`.


