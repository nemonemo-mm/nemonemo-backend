package com.example.demo.repository;

import com.example.demo.domain.entity.TodoAttendee;
import com.example.demo.domain.entity.TodoAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoAttendeeRepository extends JpaRepository<TodoAttendee, TodoAttendeeId> {
}







