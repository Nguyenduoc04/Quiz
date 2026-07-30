package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.UserRegisterDTO; // <-- Bổ sung import DTO đăng ký
import com.codegym.Quiz.dto.UserUpdateDTO;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void registerNewUser(UserRegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword()); // Sau này tích hợp Spring Security sẽ encode password tại đây
        user.setEmail(registerDTO.getEmail());
        user.setFullName(registerDTO.getFullName());
        user.setEnabled(true);

        userRepository.save(user);
    }

    // Lấy thông tin user hiện tại đổi sang DTO để điền sẵn vào Form
    public UserUpdateDTO getUserProfileForEdit(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        return new UserUpdateDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail()
        );
    }

    //Kiểm tra Email mới có bị trùng với tài khoản của NGƯỜI KHÁC hay không
    public boolean isEmailTakenByAnotherUser(String email, Long currentUserId) {
        Optional<User> userWithEmail = userRepository.findByEmail(email);
        // Trùng email NẾU tìm thấy user KHÁC id hiện tại
        return userWithEmail.isPresent() && !userWithEmail.get().getId().equals(currentUserId);
    }

    //Cập nhật thông tin User vào Database
    public void updateUserProfile(UserUpdateDTO updateDTO) {
        User user = userRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setFullName(updateDTO.getFullName());
        user.setEmail(updateDTO.getEmail());

        userRepository.save(user);
    }
}