package com.example.qrgenerator.repository;

import com.example.qrgenerator.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    List<QRCode> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<QRCode> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
