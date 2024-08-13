package com.example.task.service;

import com.example.task.VO.StatResponse;
import com.example.task.model.Stats;
import com.example.task.model.Task;
import com.example.task.repository.StatsRepository;
import com.example.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private StatsRepository statsRepository;
    public List<StatResponse> calculateStats(LocalDate startDate, LocalDate endDate, int teamId) {
        // Retrieve tasks within the date range and for the specified team
        List<Task> tasks = taskRepository.findByCreatedDateBetweenAndTeamId(startDate, endDate, (long) teamId);

        // Group tasks by engineerId
        Map<Long, List<Task>> tasksByEngineer = tasks.stream()
                .collect(Collectors.groupingBy(Task::getEngineerId));

        // Prepare the list to store statistics per engineer
        List<StatResponse> stats = new ArrayList<>();

        for (Map.Entry<Long, List<Task>> entry : tasksByEngineer.entrySet()) {
            Long engineerId = entry.getKey();
            List<Task> engineerTasks = entry.getValue();

            // Initialize counters for each task type
            int p1Count = 0;
            int stcCount = 0;
            int qmCount = 0;
            int chatCount = 0;

            // Count the tasks by type
            for (Task task : engineerTasks) {
                switch (task.getName()) {
                    case "p1":
                        p1Count++;
                        break;
                    case "stc":
                        stcCount++;
                        break;
                    case "qm":
                        qmCount++;
                        break;
                    case "chat":
                        chatCount++;
                        break;
                }
            }

            // Calculate the credit
            int score = p1Count * 2 + qmCount + stcCount + chatCount;

            // Create StatResponse for the engineer
            StatResponse statResponse = new StatResponse(
                    engineerId.intValue(),
                    teamId,
                    p1Count,
                    stcCount,
                    chatCount,
                    qmCount,
                    score
            );

            // Add the stat response to the list
            stats.add(statResponse);
        }

        return stats;
    }
    public List<StatResponse> calculateStatsForCurrentQuarter(int teamId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfQuarter = today.with(today.getMonth().firstMonthOfQuarter())
                .with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastWorkingDay = calculateLastWorkingDay(LocalDate.now());

        // Fetch stats from repository for the current quarter
        List<Stats> existingStats = statsRepository.findByStartDateAndTeamID(startOfQuarter, teamId);

        if (!existingStats.isEmpty()) {
            boolean allStatsUpToDate = true;

            for (Stats stat : existingStats) {
                if (stat.getEndDate().isBefore(lastWorkingDay)) {
                    // Case 2: Calculate and update stats from statEndDate to lastWorkingDay
                    List<StatResponse> newStats = calculateStats(stat.getEndDate().plusDays(1), lastWorkingDay, teamId);
                    updateExistingStat(stat, newStats);
                    allStatsUpToDate = false;
                }
            }

            if (allStatsUpToDate) {
                // Case 1: Return existing stats
                return convertToStatResponse(existingStats);
            }

            // After updating, return the newly updated stats
            return convertToStatResponse(existingStats);
        }

        // Case 3: No stats exist, calculate from start of quarter to last working day
        List<StatResponse> stats = calculateStats(startOfQuarter, lastWorkingDay, teamId);
        saveNewStats(stats, startOfQuarter, lastWorkingDay);
        return stats;
    }

    private LocalDate calculateLastWorkingDay(LocalDate date) {
        // Logic to calculate the last working day before the given date
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.minusDays(1);
            case SUNDAY:
                return date.minusDays(2);
            default:
                return date;
        }
    }

    private void updateExistingStat(Stats stat, List<StatResponse> newStats) {
        for (StatResponse response : newStats) {
            if (stat.getEngineerId() == response.getEngineerId()) {
                stat.setP1(stat.getP1() + response.getP1());
                stat.setStc(stat.getStc() + response.getStc());
                stat.setChat(stat.getChat() + response.getChat());
                stat.setQm(stat.getQm() + response.getQm());
                stat.setScore(stat.getScore() + response.getScore());
                stat.setEndDate(LocalDate.now());
                statsRepository.save(stat);
                break;  // Once the matching stat is found and updated, break out of the loop
            }
        }
    }

    private void saveNewStats(List<StatResponse> stats, LocalDate startOfQuarter, LocalDate endOfPeriod) {
        for (StatResponse response : stats) {
            Stats newStat = new Stats();
            newStat.setEngineerId(response.getEngineerId());
            newStat.setTeamID(response.getTeamID());
            newStat.setP1(response.getP1());
            newStat.setStc(response.getStc());
            newStat.setChat(response.getChat());
            newStat.setQm(response.getQm());
            newStat.setScore(response.getScore());
            newStat.setStartDate(startOfQuarter);
            newStat.setEndDate(endOfPeriod);
            statsRepository.save(newStat);
        }
    }

    private List<StatResponse> convertToStatResponse(List<Stats> stats) {
        return stats.stream()
                .map(stat -> new StatResponse(
                        stat.getEngineerId(),
                        stat.getTeamID(),
                        stat.getP1(),
                        stat.getStc(),
                        stat.getChat(),
                        stat.getQm(),
                        stat.getScore()))
                .collect(Collectors.toList());
    }
    public void resetAndRecalculateStatsForCurrentQuarter(int teamId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfQuarter = today.with(today.getMonth().firstMonthOfQuarter())
                .with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastWorkingDay = calculateLastWorkingDay(today);

        // Fetch and delete existing stats from the repository for the current quarter
        List<Stats> existingStats = statsRepository.findByStartDateAndTeamID(startOfQuarter, teamId);
        if (!existingStats.isEmpty()) {
            statsRepository.deleteAll(existingStats);
        }

        // Recalculate the stats for the current quarter
        List<StatResponse> newStats = calculateStats(startOfQuarter, lastWorkingDay, teamId);

        // Save the newly calculated stats
        saveNewStats(newStats, startOfQuarter, lastWorkingDay);
    }
}
