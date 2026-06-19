package com.warehouse.config;

import com.warehouse.entity.User;
import com.warehouse.entity.User.Role;
import com.warehouse.entity.User.UserStatus;
import com.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 创建默认管理员账户
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .realName("系统管理员")
                        .phone("13800000000")
                        .email("admin@warehouse.com")
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(admin);
            }
            
            // 创建测试用户
            if (!userRepository.existsByUsername("user")) {
                User user = User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user123"))
                        .realName("测试用户")
                        .phone("13800000001")
                        .email("user@warehouse.com")
                        .role(Role.USER)
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(user);
            }
        };
    }
}
