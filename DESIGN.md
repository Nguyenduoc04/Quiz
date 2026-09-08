# 🎨 Quiz CodeGym - Quy Chuẩn Thiết Kế & Giao Diện (DESIGN.md)

Tài liệu này quy định màu sắc chủ đạo, thiết kế chuẩn và quy tắc UI/UX thống nhất cho tất cả các trang web thuộc hệ thống **Quiz CodeGym**. Tất cả các trang HTML, CSS và Component phải tuân thủ nghiêm ngặt bảng thiết kế này.

---

## 1. Bảng Màu Chủ Đạo (Unified Color Palette)

Hệ thống sử dụng tông màu chủ đạo **Indigo & Blue Accent** hiện đại, kết hợp với các màu cảnh báo chuẩn theo nguyên tắc UI/UX.

| Vai trò màu | Tên màu | Giá trị HEX | Sử dụng |
| :--- | :--- | :--- | :--- |
| **Primary (Chủ đạo chính)** | Indigo | `#4F46E5` / `#6366F1` | Nút bấm chính, Header banner, Icon nổi bật, Focus ring |
| **Primary Hover / Dark** | Deep Indigo | `#4338CA` / `#3730A3` | Trạng thái Hover / Active của nút chính |
| **Secondary (Phụ trợ)** | Vibrant Violet | `#7C3AED` / `#8B5CF6` | Gradient điểm nhấn, Bảng xếp hạng, Thẻ đặc biệt |
| **Success (Thành công)** | Emerald Green | `#10B981` / `#22C55E` | Đáp án đúng, Đã nộp bài, Thí sinh online, Nút bắt đầu |
| **Warning (Cảnh báo)** | Amber Gold | `#F59E0B` / `#FACC15` | Đang chờ, Đang làm bài, Cảnh báo thời gian |
| **Danger (Nguy hiểm)** | Rose Red | `#EF4444` / `#DC2626` | Đáp án sai, Nút Rời/Kết thúc phòng, Lỗi nhập liệu |
| **Info (Thông tin)** | Sky Blue | `#0EA5E9` / `#38BDF8` | Thông tin phụ, Hướng dẫn, Badge trạng thái |

---

## 2. Nền Trang & Khung Thẻ (Background & Surface Colors)

### Chế độ Sáng (Light Theme Default) - Dành cho User, Exam Search, Results
- **Nền trang (`body`)**: `#F0F4F8` (Xám xanh nhạt)
- **Nền thẻ (`card / container`)**: `#FFFFFF` (Trắng tinh)
- **Viền thẻ (`border`)**: `#E2E8F0` (1px solid)
- **Bóng đổ (`box-shadow`)**: `0 4px 15px rgba(0, 0, 0, 0.04)`

### Chế độ Tối (Dark Monitor Theme) - Dành cho Giám sát Phòng thi (Teacher Monitor)
- **Nền trang (`body`)**: `#0F172A` (Slate 900)
- **Nền thẻ (`card / container`)**: `#1E293B` (Slate 800)
- **Viền thẻ (`border`)**: `#334155` (1px solid)
- **Bóng đổ (`box-shadow`)**: `0 10px 30px rgba(0, 0, 0, 0.3)`

---

## 3. Quy Chuẩn Font Chữ & Phông Chữ (Typography)

- **Font Family**: `'Segoe UI', system-ui, -apple-system, BlinkMacSystemFont, 'Roboto', 'Inter', sans-serif`
- **Tiêu đề (`h1, h2, h3`)**: `font-weight: 700` hoặc `800`, màu `#0F172A` (Sáng) hoặc `#FFFFFF` (Tối)
- **Văn bản nội dung (`body, p`)**: `font-weight: 400`, màu `#334155` (Sáng) hoặc `#E2E8F0` (Tối)
- **Ghi chú / Muted text**: `color: #64748B` (Sáng) hoặc `#94A3B8` (Tối)

---

## 4. Quy Chuẩn Linh Kiện UI (Component Design Standards)

### A. Nút bấm (Buttons)
- **Bo góc (`border-radius`)**: `12px`
- **Kiểu chữ (`font-weight`)**: `600` hoặc `700`, font-size `0.9rem` - `1rem`
- **Hiệu ứng (`transition`)**: `0.2s ease`
- **Nút Chính (`btn-primary`)**: Nền `#4F46E5`, chữ trắng, shadow nhẹ. Hover background `#4338CA`.
- **Nút Phụ (`btn-outline`)**: Nền `#FFFFFF`, viền `#CBD5E1`, chữ `#475569`. Hover background `#F8FAFC`.
- **Nút Nguy hiểm (`btn-danger`)**: Nền `#EF4444`, chữ trắng. Hover background `#DC2626`.

### B. Nhãn Trạng Thái (Badges & Status Pills)
- **Bo góc (`border-radius`)**: `12px` hoặc `20px`
- **Padding**: `0.35rem 0.8rem`
- **Font**: `font-weight: 700`, size `0.8rem` - `0.85rem`
- Có icon minh họa đi kèm (`Bootstrap Icons`).

### C. Ô Nhập Liệu (Input Fields)
- **Bo góc (`border-radius`)**: `10px` - `12px`
- **Padding**: `0.75rem 1rem`
- **Viền**: `1px solid #CBD5E1`
- **Focus**: Border-color `#6366F1`, Box-shadow `0 0 0 4px rgba(99, 102, 241, 0.18)`

---

## 5. Danh Sách Biến CSS Chuẩn (CSS Custom Properties - `:root`)

```css
:root {
    /* Main Palette */
    --primary: #4f46e5;
    --primary-hover: #4338ca;
    --primary-light: #e0e7ff;
    --secondary: #7c3aed;
    --secondary-hover: #6d28d9;
    
    /* Status Colors */
    --success: #10b981;
    --success-light: #dcfce7;
    --warning: #f59e0b;
    --warning-light: #fef3c7;
    --danger: #ef4444;
    --danger-light: #fee2e2;
    --info: #0ea5e9;
    --info-light: #e0f2fe;

    /* Light Theme Surface */
    --bg-main: #f0f4f8;
    --bg-card: #ffffff;
    --border-color: #e2e8f0;
    --text-main: #0f172a;
    --text-muted: #64748b;

    /* Dark Theme Surface */
    --dark-bg-main: #0f172a;
    --dark-bg-card: #1e293b;
    --dark-border: #334155;
    --dark-text-main: #f8fafc;
    --dark-text-muted: #94a3b8;

    /* Radii & Shadows */
    --radius-sm: 8px;
    --radius-md: 12px;
    --radius-lg: 20px;
    --shadow-sm: 0 2px 8px rgba(0,0,0,0.04);
    --shadow-md: 0 4px 15px rgba(0,0,0,0.05);
    --shadow-lg: 0 10px 25px rgba(0,0,0,0.1);
}
```
