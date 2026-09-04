package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.dto.ExamDetailDTO;
import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamResult;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.ExamResultRepository;
import com.codegym.Quiz.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class OnlineExamController {

    private final ExamService examService;
    private final AuthenticationHelper authHelper;

    // In-memory thread-safe room store for online exam rooms
    private static final Map<String, OnlineRoom> roomStore = new ConcurrentHashMap<>();

    public static class ParticipantResult {
        private String studentName;
        private boolean submitted;
        private double score;
        private int correctCount;
        private int totalQuestions;
        private String submitTime;

        public ParticipantResult(String studentName) {
            this.studentName = studentName;
            this.submitted = false;
            this.score = 0.0;
            this.correctCount = 0;
            this.totalQuestions = 0;
            this.submitTime = "-";
        }

        public String getStudentName() { return studentName; }
        public boolean isSubmitted() { return submitted; }
        public void setSubmitted(boolean submitted) { this.submitted = submitted; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public int getCorrectCount() { return correctCount; }
        public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public String getSubmitTime() { return submitTime; }
        public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }
    }

    public static class OnlineRoom {
        private String pin;
        private Long examId;
        private String roomName;
        private String teacherName;
        private Integer durationMinutes;
        private String status; // "WAITING", "IN_PROGRESS", "FINISHED"
        private List<String> participants = new CopyOnWriteArrayList<>();
        private Map<String, ParticipantResult> results = new ConcurrentHashMap<>();

        public OnlineRoom(String pin, Long examId, String roomName, String teacherName, Integer durationMinutes) {
            this.pin = pin;
            this.examId = examId;
            this.roomName = roomName;
            this.teacherName = teacherName;
            this.durationMinutes = durationMinutes;
            this.status = "WAITING";
        }

        public String getPin() { return pin; }
        public Long getExamId() { return examId; }
        public String getRoomName() { return roomName; }
        public String getTeacherName() { return teacherName; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<String> getParticipants() { return participants; }
        public Map<String, ParticipantResult> getResults() { return results; }

        public void addParticipant(String name) {
            if (name != null && !name.isBlank()) {
                if (name.contains("(Giáo viên)") || (teacherName != null && name.equalsIgnoreCase(teacherName))) {
                    return; // Không thêm giáo viên vào danh sách thí sinh làm bài
                }
                if (!participants.contains(name)) {
                    participants.add(name);
                    results.putIfAbsent(name, new ParticipantResult(name));
                }
            }
        }

        public void recordResult(String studentName, double score, int correctCount, int totalQuestions) {
            ParticipantResult res = results.computeIfAbsent(studentName, ParticipantResult::new);
            res.setSubmitted(true);
            res.setScore(score);
            res.setCorrectCount(correctCount);
            res.setTotalQuestions(totalQuestions);
            res.setSubmitTime(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    public static void recordParticipantResult(String pin, String studentName, double score, int correctCount, int totalQuestions) {
        if (pin == null || pin.isBlank()) return;
        String cleanPin = pin.replace("-", "").trim();
        OnlineRoom room = roomStore.get(cleanPin);
        if (room != null && studentName != null) {
            room.recordResult(studentName, score, correctCount, totalQuestions);
        }
    }

    private final ExamResultRepository examResultRepository;

    public static class LeaderboardItem {
        private int rank;
        private String studentName;
        private double score;
        private int correctCount;
        private int totalQuestions;
        private String submitTime;
        private boolean submitted;
        private boolean isMe;

        public LeaderboardItem(int rank, String studentName, double score, int correctCount, int totalQuestions, String submitTime, boolean submitted, boolean isMe) {
            this.rank = rank;
            this.studentName = studentName;
            this.score = score;
            this.correctCount = correctCount;
            this.totalQuestions = totalQuestions;
            this.submitTime = submitTime;
            this.submitted = submitted;
            this.isMe = isMe;
        }

        public int getRank() { return rank; }
        public String getStudentName() { return studentName; }
        public double getScore() { return score; }
        public int getCorrectCount() { return correctCount; }
        public int getTotalQuestions() { return totalQuestions; }
        public String getSubmitTime() { return submitTime; }
        public boolean isSubmitted() { return submitted; }
        public boolean isIsMe() { return isMe; }
    }

    public OnlineExamController(ExamService examService, AuthenticationHelper authHelper, ExamResultRepository examResultRepository) {
        this.examService = examService;
        this.authHelper = authHelper;
        this.examResultRepository = examResultRepository;
    }

    /**
     * Giáo viên: Form tạo phòng thi trực tuyến
     */
    @GetMapping("/teacher/exam/create-online-room")
    public String showCreateOnlineRoomForm(Model model) {
        model.addAttribute("exams", examService.getOnlineAvailableExams());
        return "teacher/exam/create-online-room";
    }

    /**
     * Giáo viên: Khởi tạo phòng thi trực tuyến & sinh mã PIN 6 chữ số ngẫu nhiên
     */
    @PostMapping({"/teacher/exam/create-online-room", "/teacher/rooms/create"})
    public String createOnlineRoom(
            @RequestParam("examId") Long examId,
            @RequestParam(value = "roomName", required = false) String roomName,
            @RequestParam(value = "roomCode", required = false) String roomCode,
            @RequestParam(value = "duration", required = false) Integer duration,
            RedirectAttributes redirectAttributes) {
        try {
            Exam exam = examService.getExamById(examId);

            // Giữ nguyên trạng thái PUBLISHED của bài thi để không bị ẩn khỏi Dashboard
            if (exam.getStatus() == ExamStatus.DRAFT) {
                examService.updateStatus(examId, ExamStatus.PUBLISHED);
            }

            String pin = (roomCode != null && !roomCode.isBlank())
                    ? roomCode.replace("-", "").trim()
                    : String.format("%06d", new Random().nextInt(900000) + 100000);

            Optional<User> currentUserOpt = authHelper.getCurrentUser();
            String teacherName = currentUserOpt.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                    .orElse("Giáo viên");

            String nameOfRoom = (roomName != null && !roomName.isBlank()) ? roomName : ("Phòng thi: " + exam.getTitle());

            Integer finalDuration = (duration != null && duration > 0) ? duration : (exam.getDurationMinutes() != null ? exam.getDurationMinutes() : 45);

            OnlineRoom room = new OnlineRoom(pin, examId, nameOfRoom, teacherName, finalDuration);
            roomStore.put(pin, room);

            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo phòng thi trực tuyến thành công! Mã PIN: " + pin);
            redirectAttributes.addAttribute("pin", pin);
            redirectAttributes.addAttribute("examId", examId);
            redirectAttributes.addAttribute("studentName", teacherName);

            return "redirect:/user/waiting-room";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/exam/create-online-room";
        }
    }

    /**
     * Học sinh / Khách: Màn hình nhập mã PIN & Tên hiển thị để vào phòng thi
     */
    @GetMapping("/user/join-room")
    public String showJoinRoomForm(Model model) {
        Optional<User> currentUserOpt = authHelper.getCurrentUser();
        String userFullName = currentUserOpt.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                .orElse(null);
        model.addAttribute("userFullName", userFullName);
        return "user/join-room";
    }

    /**
     * Học sinh / Khách: Xử lý nhập mã PIN & Tên hiển thị -> Chuyển sang phòng chờ
     */
    @PostMapping("/user/join-room")
    public String processJoinRoom(
            @RequestParam("pin") String pin,
            @RequestParam(value = "studentName", required = false) String studentName,
            RedirectAttributes redirectAttributes) {

        if (pin == null || pin.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã PIN không được để trống.");
            return "redirect:/user/join-room";
        }

        String cleanPin = pin.replace("-", "").trim();

        Optional<User> currentUserOpt = authHelper.getCurrentUser();
        String name = (studentName != null && !studentName.isBlank()) ? studentName
                : currentUserOpt.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername()).orElse("Thí sinh vãng lai");

        OnlineRoom room = roomStore.get(cleanPin);
        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã phòng thi không tồn tại hoặc đã kết thúc. Vui lòng kiểm tra lại mã PIN!");
            return "redirect:/user/join-room";
        }

        room.addParticipant(name);

        redirectAttributes.addAttribute("pin", cleanPin);
        redirectAttributes.addAttribute("examId", room.getExamId());
        redirectAttributes.addAttribute("studentName", name);

        return "redirect:/user/waiting-room";
    }

    /**
     * Màn hình phòng chờ trực tuyến (Waiting Room)
     */
    @GetMapping("/user/waiting-room")
    public String showWaitingRoom(
            @RequestParam(value = "pin", required = false) String pin,
            @RequestParam(value = "examId", required = false) Long examId,
            @RequestParam(value = "studentName", required = false) String studentName,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (pin == null || pin.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mã PIN phòng thi!");
            return "redirect:/user/join-room";
        }

        String cleanPin = pin.replace("-", "").trim();

        Optional<User> currentUserOpt = authHelper.getCurrentUser();
        String name = (studentName != null && !studentName.isBlank()) ? studentName
                : currentUserOpt.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername()).orElse("Thí sinh");

        OnlineRoom room = roomStore.get(cleanPin);
        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã phòng thi không tồn tại hoặc đã kết thúc. Vui lòng kiểm tra lại mã PIN!");
            return "redirect:/user/join-room";
        }

        boolean isTeacher = authHelper.isTeacher() || authHelper.isAdmin();
        if (!isTeacher) {
            room.addParticipant(name);
        }

        model.addAttribute("pin", cleanPin);
        model.addAttribute("examId", room.getExamId());
        model.addAttribute("roomName", room.getRoomName());
        model.addAttribute("teacherName", room.getTeacherName());
        model.addAttribute("durationMinutes", room.getDurationMinutes());
        model.addAttribute("studentName", name);
        model.addAttribute("participants", room.getParticipants());
        model.addAttribute("isTeacher", isTeacher);
        model.addAttribute("status", room.getStatus());

        return "user/waiting-room";
    }

    /**
     * Giáo viên bấm "Bắt đầu làm bài" -> Kích hoạt IN_PROGRESS và chuyển Giáo viên sang màn hình Giám sát (Monitor)
     */
    @PostMapping("/user/start-online-exam")
    public String startOnlineExam(@RequestParam("pin") String pin, RedirectAttributes redirectAttributes) {
        String cleanPin = pin.replace("-", "").trim();
        OnlineRoom room = roomStore.get(cleanPin);
        if (room != null) {
            room.setStatus("IN_PROGRESS");
            if (authHelper.isTeacher() || authHelper.isAdmin()) {
                redirectAttributes.addAttribute("pin", cleanPin);
                return "redirect:/teacher/room-monitor";
            }
            redirectAttributes.addAttribute("examId", room.getExamId());
            redirectAttributes.addAttribute("pin", cleanPin);
            return "redirect:/user/take-online";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng thi.");
        return "redirect:/user/join-room";
    }

    /**
     * API kiểm tra trạng thái phòng thi (phục vụ Polling JS tại phòng chờ)
     */
    @GetMapping("/api/room-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRoomStatus(@RequestParam("pin") String pin) {
        String cleanPin = pin.replace("-", "").trim();
        OnlineRoom room = roomStore.get(cleanPin);
        Map<String, Object> response = new HashMap<>();
        if (room != null) {
            response.put("status", room.getStatus());
            response.put("examId", room.getExamId());
            response.put("participantsCount", room.getParticipants().size());
        } else {
            response.put("status", "WAITING");
            response.put("examId", 1L);
            response.put("participantsCount", 1);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Màn hình Giám sát bài thi trực tuyến dành riêng cho Giáo viên (Teacher Live Monitor)
     */
    @GetMapping("/teacher/room-monitor")
    public String showRoomMonitor(
            @RequestParam("pin") String pin,
            Model model,
            RedirectAttributes redirectAttributes) {

        String cleanPin = (pin != null) ? pin.replace("-", "").trim() : "";
        OnlineRoom room = roomStore.get(cleanPin);
        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phòng thi không tồn tại!");
            return "redirect:/dashboard";
        }

        try {
            Exam exam = examService.getExamById(room.getExamId());
            model.addAttribute("examTitle", exam.getTitle());
        } catch (Exception e) {
            model.addAttribute("examTitle", room.getRoomName());
        }

        model.addAttribute("pin", cleanPin);
        model.addAttribute("room", room);
        model.addAttribute("results", room.getResults().values());

        return "teacher/exam/room-monitor";
    }

    /**
     * API Polling dữ liệu Giám sát Realtime của Giáo viên
     */
    @GetMapping("/api/room-monitor-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRoomMonitorData(@RequestParam("pin") String pin) {
        String cleanPin = (pin != null) ? pin.replace("-", "").trim() : "";
        OnlineRoom room = roomStore.get(cleanPin);
        Map<String, Object> response = new HashMap<>();

        if (room != null) {
            response.put("status", room.getStatus());
            response.put("pin", room.getPin());
            response.put("roomName", room.getRoomName());
            response.put("teacherName", room.getTeacherName());
            response.put("durationMinutes", room.getDurationMinutes());
            response.put("results", room.getResults().values());
            response.put("totalParticipants", room.getResults().size());

            long submittedCount = room.getResults().values().stream().filter(ParticipantResult::isSubmitted).count();
            response.put("submittedCount", submittedCount);
        } else {
            response.put("status", "NOT_FOUND");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Giáo viên bấm "Kết thúc bài thi ngay"
     */
    @PostMapping("/teacher/finish-online-exam")
    public String finishOnlineExam(@RequestParam("pin") String pin, RedirectAttributes redirectAttributes) {
        String cleanPin = (pin != null) ? pin.replace("-", "").trim() : "";
        OnlineRoom room = roomStore.get(cleanPin);
        if (room != null) {
            room.setStatus("FINISHED");
        }
        redirectAttributes.addAttribute("pin", cleanPin);
        return "redirect:/teacher/room-monitor";
    }

    /**
     * Màn hình làm bài thi trực tuyến Realtime (Dành cho Học sinh)
     */
    @GetMapping("/user/take-online")
    public String takeOnlineExam(
            @RequestParam(value = "examId", defaultValue = "1") Long examId,
            @RequestParam(value = "pin", required = false) String pin,
            @RequestParam(value = "studentName", required = false) String studentName,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Nếu là giáo viên thì tự động chuyển hướng sang màn hình Giám sát
        if (authHelper.isTeacher() || authHelper.isAdmin()) {
            if (pin != null && !pin.isBlank()) {
                redirectAttributes.addAttribute("pin", pin.replace("-", "").trim());
                return "redirect:/teacher/room-monitor";
            }
        }

        try {
            ExamDetailDTO examDetail = examService.getExamDetailForStudent(examId);
            Optional<User> currentUserOpt = authHelper.getCurrentUser();
            String name = (studentName != null && !studentName.isBlank()) ? studentName
                    : currentUserOpt.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername()).orElse("Thí sinh");

            String cleanPin = (pin != null) ? pin.replace("-", "").trim() : "";
            OnlineRoom room = roomStore.get(cleanPin);
            List<String> participants = (room != null) ? room.getParticipants() : List.of(name);
            String roomName = (room != null) ? room.getRoomName() : examDetail.getTitle();

            Integer roomDuration = (room != null && room.getDurationMinutes() != null && room.getDurationMinutes() > 0)
                    ? room.getDurationMinutes()
                    : (examDetail.getDurationMinutes() != null ? examDetail.getDurationMinutes() : 45);

            model.addAttribute("examId", examDetail.getId());
            model.addAttribute("examTitle", examDetail.getTitle());
            model.addAttribute("durationMinutes", roomDuration);
            model.addAttribute("questions", examDetail.getQuestions());
            model.addAttribute("studentName", name);
            model.addAttribute("pin", cleanPin);
            model.addAttribute("roomName", roomName);
            model.addAttribute("participants", participants);

            return "user/take-online";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/join-room";
        }
    }

    /**
     * Màn hình tự động nộp bài khi hết thời gian thi Online
     */
    @GetMapping("/user/exam-auto-submit")
    public String autoSubmitScreen(
            @RequestParam(value = "examId", defaultValue = "1") Long examId,
            Model model) {
        model.addAttribute("examId", examId);
        return "user/exam-auto-submit";
    }

    /**
     * Màn hình Bảng xếp hạng thi trực tuyến (Leaderboard Realtime)
     */
    @GetMapping("/user/online-result")
    public String showOnlineResult(
            @RequestParam(value = "pin", required = false) String pin,
            @RequestParam(value = "examId", required = false) Long examId,
            @RequestParam(value = "studentName", required = false) String studentName,
            Model model) {

        final Long targetExamId = examId;
        String cleanPin = (pin != null) ? pin.replace("-", "").trim() : "";
        OnlineRoom room = !cleanPin.isEmpty() ? roomStore.get(cleanPin) : null;
        if (room == null && targetExamId != null) {
            room = roomStore.values().stream()
                    .filter(r -> r.getExamId() != null && r.getExamId().equals(targetExamId))
                    .findFirst().orElse(null);
        }

        Optional<User> currentUserOpt = authHelper.getCurrentUser();
        String currentUserName = (studentName != null && !studentName.isBlank())
                ? studentName
                : currentUserOpt.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername()).orElse("");

        List<LeaderboardItem> rankingList = new ArrayList<>();
        LeaderboardItem myResult = null;
        int myRank = 0;

        if (room != null) {
            model.addAttribute("pin", room.getPin());
            model.addAttribute("roomName", room.getRoomName());
            if (examId == null) examId = room.getExamId();

            List<ParticipantResult> roomResults = new ArrayList<>(room.getResults().values());
            // Ưu tiên: Đã nộp bài -> Điểm cao xuống thấp -> Thời gian nộp sớm xếp trên
            roomResults.sort((a, b) -> {
                if (a.isSubmitted() != b.isSubmitted()) {
                    return a.isSubmitted() ? -1 : 1;
                }
                int scoreCmp = Double.compare(b.getScore(), a.getScore());
                if (scoreCmp != 0) return scoreCmp;
                if (a.getSubmitTime() != null && b.getSubmitTime() != null) {
                    return a.getSubmitTime().compareTo(b.getSubmitTime());
                }
                return 0;
            });

            int rank = 1;
            for (ParticipantResult res : roomResults) {
                boolean isMe = !currentUserName.isEmpty() && res.getStudentName().equalsIgnoreCase(currentUserName);
                LeaderboardItem item = new LeaderboardItem(
                        rank,
                        res.getStudentName(),
                        res.getScore(),
                        res.getCorrectCount(),
                        res.getTotalQuestions(),
                        res.getSubmitTime(),
                        res.isSubmitted(),
                        isMe
                );
                rankingList.add(item);
                if (isMe) {
                    myResult = item;
                    myRank = rank;
                }
                rank++;
            }
        } else if (examId != null) {
            // Lấy dữ liệu thực tế từ CSDL khi không tìm thấy phòng thi online tạm thời
            List<ExamResult> dbResults = examResultRepository.findByExamIdOrderByScoreDescSubmittedAtAsc(examId);
            int rank = 1;
            for (ExamResult er : dbResults) {
                String uName = (er.getUser() != null)
                        ? (er.getUser().getFullName() != null ? er.getUser().getFullName() : er.getUser().getUsername())
                        : "Thí sinh";
                boolean isMe = !currentUserName.isEmpty() && uName.equalsIgnoreCase(currentUserName);
                String subTime = (er.getSubmittedAt() != null)
                        ? er.getSubmittedAt().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
                        : "-";

                LeaderboardItem item = new LeaderboardItem(
                        rank,
                        uName,
                        er.getScore(),
                        er.getCorrectCount(),
                        er.getTotalQuestions(),
                        subTime,
                        true,
                        isMe
                );
                rankingList.add(item);
                if (isMe && myResult == null) {
                    myResult = item;
                    myRank = rank;
                }
                rank++;
            }
        }

        if (examId != null) {
            try {
                Exam exam = examService.getExamById(examId);
                model.addAttribute("exam", exam);
            } catch (Exception ignored) {}
        }

        model.addAttribute("rankingList", rankingList);
        model.addAttribute("myResult", myResult);
        model.addAttribute("myRank", myRank);

        return "user/online-result";
    }
}
