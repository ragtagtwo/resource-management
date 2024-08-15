package com.example.task.controller;

import com.example.task.VO.StatResponse;
import com.example.task.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class StatController {
    @Autowired
    private StatService statService;

    @GetMapping("/stats")
    public List<StatResponse> getStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("teamId") int teamId) {

        return statService.calculateStats(startDate, endDate, teamId);
    }
    @GetMapping("/stats/current-quarter")
    public List<StatResponse> getStatsForCurrentQuarter(
            @RequestParam("teamId") int teamId) {
        return statService.calculateStatsForCurrentQuarter(teamId);
    }

    @PutMapping("/stats/current-quarter/update")
    public List<StatResponse> updateStatsForCurrentQuarter(
            @RequestParam("teamId") int teamId) {
        statService.resetAndRecalculateStatsForCurrentQuarter(teamId);
        return statService.calculateStatsForCurrentQuarter(teamId);
    }
}
