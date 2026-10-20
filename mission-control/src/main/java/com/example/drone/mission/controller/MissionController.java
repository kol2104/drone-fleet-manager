package com.example.drone.mission.controller;

import com.example.drone.common.dto.MissionDto;
import com.example.drone.mission.model.MissionStatusResponse;
import com.example.drone.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    public ResponseEntity<MissionDto> createMission(@RequestBody MissionDto missionRequest) {
        MissionDto mission = missionService.startMission(missionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(mission);
    }

    @GetMapping("/{processInstanceKey}/status")
    public ResponseEntity<MissionStatusResponse> getMissionStatus(@PathVariable long processInstanceKey) {
        return ResponseEntity.ok(missionService.getMissionStatus(processInstanceKey));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Mission Control running on port 8083");
    }
}
