package com.example.demo.repository;

import com.example.demo.domain.entity.Alert;
import com.example.demo.domain.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    List<Alert> findTop50ByUserIdAndTypeInOrderByCreatedAtDesc(Long userId, List<AlertType> types);
}


