package com.example.drone.mission.controller;

import com.example.drone.mission.service.MissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class MissionExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "BAD_REQUEST",
                "message", exception.getMessage()
        ));
    }

    @ExceptionHandler(MissionService.MissionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            MissionService.MissionNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "MISSION_NOT_FOUND",
                "message", exception.getMessage()
        ));
    }
}
