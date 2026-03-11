package com.flowhr.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Menjalankan Database Seeder...");

        // Update password superadmin menggunakan PasswordEncoder bawaan Spring Security
        String rawPassword = "Admin@FlowHR2024";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        int updated = jdbc.update("UPDATE users SET password = ? WHERE username = 'superadmin'", encodedPassword);

        if (updated > 0) {
            log.info("✅ Berhasil mengatur ulang password 'superadmin' ke: {}", rawPassword);
        } else {
            log.warn("❌ User 'superadmin' tidak ditemukan. Pastikan migrasi Flyway V1 sudah berjalan.");
        }
    }
}
