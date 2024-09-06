package com.example.task.service;

import com.example.task.VO.Engineer;
import com.example.task.model.Task;
import com.example.task.model.Vacation;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.VacationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.* ;

@Service
public class TaskDistribution {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private VacationRepository vacationRepository;

    public void distributeAll(Long teamId, Integer stc) {
        RestTemplate restTemplate = new RestTemplate();
        String engineersUrl = "http://localhost:8081/api/engineers/team/" + teamId;
        ResponseEntity<List<Engineer>> response = restTemplate.exchange(
                engineersUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Engineer>>() {}
        );
        System.out.println("fetched engineers ok");

        List<Engineer> engineers = response.getBody();

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 3);

        // Distribute STC tasks based on the provided day
        distributeSTCTasks(engineers, startDate, endDate, stc);
        System.out.println("stc ok");

        distributeP1Tasks(engineers, startDate, endDate);
        System.out.println("p1 ok");
        distributeChatTasks(engineers, startDate, endDate);
    }
@Transactional
    public void equilibrate(int Type, Long teamId) {
        RestTemplate restTemplate = new RestTemplate();
        String engineersUrl = "http://localhost:8081/api/engineers/team/" + teamId;
        ResponseEntity<List<Engineer>> response = restTemplate.exchange(
                engineersUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Engineer>>() {}
        );

        List<Engineer> engineers = response.getBody();

        if (Type == 1) {
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.MONTH, 3);
            LocalDate sDate = LocalDate.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
            LocalDate eDate = LocalDate.ofInstant(endDate.toInstant(), ZoneId.systemDefault()); // Example: end date is a week from the start date
            redistributeChatTasks(engineers, sDate, eDate, teamId);
        }

        // You can handle other cases for different `Type` values here if needed
    }

    @Transactional
    public void reset(Long teamId) {
        LocalDate today = LocalDate.now();
        taskRepository.deleteByCreatedDateGreaterThanEqualAndNameInAndTeamId(today, List.of("p1", "chat","stc"), teamId);
    }
    public void distributeP1Tasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        LocalDate currentDate = LocalDate.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDate endLocalDate = LocalDate.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        Long teamId = engineers.get(0).getTeamId();
        // Set up the algorithm for task distribution
        algorithmSetUp(engineers, startDate);

        List<Engineer> vacationingEngineers = new ArrayList<>();

        while (!currentDate.isAfter(endLocalDate)) {
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                currentDate = currentDate.plusDays(1);
                continue; // Skip weekends
            }

            // Move engineers returning from vacation back to the main list
            List<Engineer> returningEngineers = new ArrayList<>();
            for (Engineer vacationingEngineer : vacationingEngineers) {
                Date currentDateAsDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (checkVacation(vacationingEngineer, currentDateAsDate)) {
                    engineers.add(vacationingEngineer);
                    returningEngineers.add(vacationingEngineer);
                }
            }
            vacationingEngineers.removeAll(returningEngineers);

            System.out.println("Date: " + currentDate);
            System.out.println("Engineers: " + engineers);

            // Convert LocalDate to Date for checkAvailabilityP1 method
            Date currentDateAsDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Check availability with teamId
            if (!checkAvailabilityP1(currentDateAsDate, teamId)) {
                System.out.println("P1 task already assigned on " + currentDate);
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // Get the first available engineer with priority 0 and p1 attribute true, and not on vacation
            Engineer engineerToAssign = engineers.stream()
                    .filter(engineer -> engineer.getPriority() == 0 && Boolean.TRUE.equals(engineer.getP1()) && checkAvailableForP1(engineer, currentDateAsDate))
                    .findFirst()
                    .orElse(null);

            if (engineerToAssign == null) {
                // If no engineer with priority 0 is available, sort engineers by p1Done and assign to one with priority 1 and p1 attribute true
                engineerToAssign = engineers.stream()
                        .sorted(Comparator.comparingInt(Engineer::getP1Done)) // Sort engineers by p1Done only
                        .filter(engineer -> engineer.getPriority() == 1 && Boolean.TRUE.equals(engineer.getP1()) && checkAvailableForP1(engineer, currentDateAsDate))
                        .findFirst()
                        .orElse(null);
            }

            if (engineerToAssign != null) {
                // Assign P1 to this engineer
                saveTask(engineerToAssign.getId(), "p1", currentDateAsDate, engineerToAssign.getTeamId());
                engineerToAssign.setPriority(1);
                engineerToAssign.setP1Done(engineerToAssign.getP1Done() + 1);

                // Check if all available engineers with p1 attribute as true have priority 1
                boolean allPrioritiesOne = engineers.stream()
                        .filter(engineer -> Boolean.TRUE.equals(engineer.getP1()))
                        .allMatch(e -> e.getPriority() == 1);

                if (allPrioritiesOne) {
                    // Reset all priorities for engineers with p1 attribute as true
                    for (Engineer engineer : engineers) {
                        if (Boolean.TRUE.equals(engineer.getP1())) {
                            engineer.setPriority(0);
                        }
                    }
                    // Reorder indexes
                    reorderIndexes(engineers);
                }
            } else {
                System.out.println("No available engineer for P1 on " + currentDate);
            }

            // Remove engineers who are on vacation for the current day
            List<Engineer> onVacationToday = new ArrayList<>();
            for (Engineer engineer : engineers) {
                if (!checkVacation(engineer, currentDateAsDate)) {
                    onVacationToday.add(engineer);
                }
            }
            engineers.removeAll(onVacationToday);
            vacationingEngineers.addAll(onVacationToday);

            currentDate = currentDate.plusDays(1);

            System.out.println("After assignment:");
            System.out.println("Engineers: " + engineers);
            System.out.println("---------------------------------");
        }
    }
    private void algorithmSetUp(List<Engineer> engineers, Calendar startDate) {
        // Check the last working day before the start date
        Calendar previousWorkingDay = getPreviousWorkingDay(startDate);
        LocalDate previousWorkingDate = LocalDate.ofInstant(previousWorkingDay.toInstant(), ZoneId.systemDefault());
        // Get the first day of the current week (Monday)
        LocalDate firstDayOfWeek = previousWorkingDate.with(DayOfWeek.MONDAY);
        // Get the teamId (assuming all engineers belong to the same team)
        Long teamId = engineers.get(0).getTeamId();

        // Get the engineer who handled the P1 task on the last working day for the specific team
        Task previousTask = taskRepository.findFirstByNameAndCreatedDateAndTeamId("p1", previousWorkingDate, teamId);
        Long previousEngineerId = (previousTask == null) ? null : previousTask.getEngineerId();

        // Find the previous engineer in the list and get their index
        int indexPreviousEngineer = -1;
        for (int i = 0; i < engineers.size(); i++) {
            if (engineers.get(i).getId().equals(previousEngineerId)) {
                indexPreviousEngineer = i;
                break;
            }
        }

        // Initialize the starting index for the engineers
        int indexInitEngineer = (indexPreviousEngineer + 1) % engineers.size();
        // Set priority of engineers with index from indexPreviousEngineer to index 0 to 1
        // Set to keep track of engineers who worked on P1 tasks during the week
        Set<Long> engineersWorkedOnP1 = new HashSet<>();

        // Iterate from the last working day back to the first day of the week
        LocalDate dateToCheck = previousWorkingDate;
        while (!dateToCheck.isBefore(firstDayOfWeek)) {
            Task task = taskRepository.findFirstByNameAndCreatedDateAndTeamId("p1", dateToCheck, teamId);
            if (task != null) {
                engineersWorkedOnP1.add(task.getEngineerId());
            }
            dateToCheck = dateToCheck.minusDays(1); // Move to the previous day
        }

        // Assign priority 1 to engineers who worked on P1 tasks during the week
        for (Engineer engineer : engineers) {
            if (engineersWorkedOnP1.contains(engineer.getId())) {
                engineer.setPriority(1);
            }
        }
        // Set the index for the engineers starting from indexInitEngineer
        for (int i = 0; i < engineers.size(); i++) {
            engineers.get((indexInitEngineer + i) % engineers.size()).setIndex(i);
        }
    }

    private void reorderIndexes(List<Engineer> engineers) {
        int size = engineers.size();
        for (int i = 0; i < size; i += 2) {
            if (i + 1 < size) {
                Engineer engineer1 = engineers.get(i);
                Engineer engineer2 = engineers.get(i + 1);
                int tempIndex = engineer1.getIndex();
                engineer1.setIndex(engineer2.getIndex());
                engineer2.setIndex(tempIndex);
            }
        }
        // Re-sort engineers by the new index
        engineers.sort(Comparator.comparingInt(Engineer::getIndex));
    }
    private boolean isWorkingDay(Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    private Calendar getPreviousWorkingDay(Calendar startDate) {
        Calendar previousWorkingDay = (Calendar) startDate.clone();
        do {
            previousWorkingDay.add(Calendar.DATE, -1);
        } while (isWeekend(previousWorkingDay));
        return previousWorkingDay;
    }

    public void distributeChatTasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        int engineerCount = engineers.size();
        Calendar date = (Calendar) startDate.clone();

        while (!date.after(endDate)) {
            if (!isWeekend(date)) {
                for (int shift = 0; shift < 2; shift++) { // 0 for morning, 1 for afternoon
                    int engineersAssigned = 0;
                    String shiftName = shift == 0 ? "morning" : "afternoon";

                    for (int j = 0; j < engineerCount && engineersAssigned < 2; j++) {
                        Engineer engineer = engineers.get((date.get(Calendar.DAY_OF_MONTH) + j + shift * 2) % engineerCount);

                        Long teamId = engineer.getTeamId();

                        // Check if the engineer is on vacation
                        if (!isEngineerOnVacation(engineer.getId(), date, shiftName) &&
                                Boolean.TRUE.equals(engineer.getChat()) &&
                                checkAvailableForChatShift(engineer, date.getTime(), shiftName) &&
                                checkAvailabilityChat(date.getTime(), shiftName, teamId)) {

                            saveChat(engineer.getId(), "chat", date.getTime(), shiftName, teamId);
                            engineersAssigned++;
                        }
                    }
                }
            }
            date.add(Calendar.DATE, 1);
        }
    }
    public boolean isEngineerOnVacation(Long engineerId, Calendar date, String shift) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<Vacation> vacations = vacationRepository.findByEngineerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                engineerId, localDate, localDate);

        // Check if there's any vacation that overlaps with the shift
        for (Vacation vacation : vacations) {
            if (vacation.getShift() == null || vacation.getShift().equals(shift) || vacation.getShift().equals("full day")) {
                return true;
            }
        }
        return false;
    }
    private boolean checkAvailableForChatShift(Engineer engineer, Date date, String shift) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Check if the engineer is on vacation during this shift
        List<Vacation> vacations = vacationRepository.findByEngineerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                engineer.getId(), localDate, localDate);
        if (!vacations.isEmpty() && vacations.stream().anyMatch(v -> shiftMatches(v, shift))) {
            return false;
        }

        // Check if the engineer has any chat tasks during the whole day
        List<Task> chatTasksAllDay = taskRepository.findByEngineerIdAndCreatedDateAndName(engineer.getId(), localDate, "chat");
        if (!chatTasksAllDay.isEmpty()) {
            return false;
        }

        // Check if the engineer has p1, qm, or stc tasks on the same day
        List<Task> otherTasks = taskRepository.findByEngineerIdAndCreatedDateAndNameIn(
                engineer.getId(), localDate, List.of("p1", "qm", "stc"));
        return otherTasks.isEmpty();
    }
    private boolean shiftMatches(Vacation vacation, String shift) {
        String vacationShift = vacation.getShift();
        return shift.equals(vacationShift) || "full day".equals(vacationShift);
    }

    private boolean checkAvailableForP1(Engineer engineer, Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // Check if the engineer is on vacation
        List<Vacation> vacations = vacationRepository.findByEngineerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                engineer.getId(), localDate, localDate);
        if (!vacations.isEmpty()) {
            return false;
        }

        // Check if the engineer has other tasks on the same day
        List<Task> tasks = taskRepository.findByEngineerIdAndCreatedDate(engineer.getId(), localDate);
        return tasks.isEmpty();
    }
    //function to check if engineer has vacation on that day

    private boolean checkVacation(Engineer engineer, Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Vacation> vacations = vacationRepository.findByEngineerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                engineer.getId(), localDate, localDate);
        return vacations.isEmpty(); // Returns true if the engineer is not on vacation
    }


    private boolean checkAvailabilityP1(Date date, Long teamId) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Task> tasks = taskRepository.findByNameAndCreatedDateAndTeamId("p1", localDate, teamId);
        return tasks.isEmpty();
    }
    private boolean checkAvailabilityChat(Date date, String shift, Long teamId) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Task> tasks = taskRepository.findByNameAndCreatedDateAndShiftAndTeamId("chat", localDate, shift, teamId);
        return tasks.size() < 2;
    }

    private void saveChat(Long engineerId, String taskName, Date date, String shift, Long teamId) {
        Task task = Task.builder()
                .name(taskName)
                .engineerId(engineerId)
                .teamId(teamId)
                .createdDate(LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                .shift(shift)
                .build();
        taskRepository.save(task);
    }

    private void saveTask(Long engineerId, String taskName, Date date, Long teamId) {
        Task task = Task.builder()
                .name(taskName)
                .engineerId(engineerId)
                .teamId(teamId)
                .createdDate(LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                .build();
        taskRepository.save(task);
    }

    private boolean isWeekend(Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
    }

    // this is the redistribute chat to equilibrate :
    @Transactional
    public void redistributeChatTasks(List<Engineer> engineers, LocalDate startDate, LocalDate endDate, Long teamId) {
        // Step 1: Calculate scores for each working day
        Map<Engineer, Integer> engineerScores = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (isWorkingDay(date)) {
                for (Engineer engineer : engineers) {
                    int p1Count = countTasks(engineer, date, "p1");
                    int stcCount = countTasks(engineer, date, "stc");
                    int qmCount = countTasks(engineer, date, "qm");
                    int chatCount = countTasks(engineer, date, "chat");

                    int score = p1Count * 2 + stcCount + qmCount + chatCount;
                    engineerScores.put(engineer, engineerScores.getOrDefault(engineer, 0) + score);
                }
            }
        }

        // Step 2: Calculate the average score
        int totalScore = engineerScores.values().stream().mapToInt(Integer::intValue).sum();
        int averageScore = totalScore / engineers.size();

        // Step 3: Iterate over each week to adjust Chat tasks for engineers with positive credit
        for (LocalDate weekStartDate = startDate; !weekStartDate.isAfter(endDate); weekStartDate = weekStartDate.plusWeeks(1)) {
            List<Task> tasksForWeek = getTasksForWeek(weekStartDate, teamId);

            System.out.println("Starting redistribution for week: " + weekStartDate);

            for (Engineer engineer : engineers) {
                int score = engineerScores.getOrDefault(engineer, 0);
                int credit = averageScore - score;
                System.out.println("Engineer: " + engineer.getName() + ", Score: " + score + ", Credit: " + credit);

                if (credit > 0) {
                    for (Task task : tasksForWeek) {
                        System.out.println("Checking task on " + task.getCreatedDate() + " for possible reassignment to " + engineer.getName());
                        if (task.getName().equals("chat") && canReplaceChat(engineer, task)) {
                            Engineer currentEngineer = getEngineerById(engineers, task.getEngineerId());

                            if (isAvailableForChat(engineer, task.getCreatedDate()) && !hasChatOnSameDay(engineer, task.getCreatedDate())) {
                                // Replace Chat task
                                System.out.println("Assigning task from " + currentEngineer.getName() + " to " + engineer.getName());
                                task.setEngineerId(engineer.getId());
                                taskRepository.save(task);

                                recalculateScore(engineerScores, engineer, currentEngineer, task.getCreatedDate());
                                credit--;

                                if (credit <= 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private void recalculateScore(Map<Engineer, Integer> engineerScores, Engineer lowScoreEngineer, Engineer currentEngineer, LocalDate date) {
        int lowScoreEngineerOldScore = engineerScores.get(lowScoreEngineer);
        int currentEngineerOldScore = engineerScores.get(currentEngineer);

        int lowScoreEngineerNewScore = lowScoreEngineerOldScore + 1; // Add 1 Chat task
        int currentEngineerNewScore = currentEngineerOldScore - 1; // Remove 1 Chat task

        engineerScores.put(lowScoreEngineer, lowScoreEngineerNewScore);
        engineerScores.put(currentEngineer, currentEngineerNewScore);
    }
    private boolean isWorkingDay(LocalDate date) {
        // Logic to define working days (e.g., exclude weekends and holidays)
        return !(date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
    }

    private int countTasks(Engineer engineer, LocalDate date, String taskType) {
        return taskRepository.findByEngineerIdAndCreatedDateAndName(engineer.getId(), date, taskType).size();
    }

    private List<Task> getTasksForWeek(LocalDate date, Long teamId) {
        LocalDate endOfWeek = date.plusDays(6);
        return taskRepository.findByCreatedDateBetweenAndTeamId(date, endOfWeek, teamId);
    }

    private boolean canReplaceChat(Engineer engineer, Task task) {
        return engineer.getChat() && !hasChatOnSameDay(engineer, task.getCreatedDate());
    }

    private boolean isAvailableForChat(Engineer engineer, LocalDate date) {
        return taskRepository.findByEngineerIdAndCreatedDateAndName(engineer.getId(), date, "chat").isEmpty();
    }

    private boolean hasChatOnSameDay(Engineer engineer, LocalDate date) {
        return taskRepository.findByEngineerIdAndCreatedDateAndName(engineer.getId(), date, "chat").size() > 0;
    }


    private Engineer getEngineerById(List<Engineer> engineers, Long engineerId) {
        // Fetch the engineer by ID from the provided list
        return engineers.stream().filter(e -> e.getId().equals(engineerId)).findFirst().orElse(null);
    }

    ////distribute stc by default on wednesday
    @Transactional
    public void distributeSTCTasks(List<Engineer> engineers, Calendar startDate, Calendar endDate, Integer stcDay) {
        LocalDate start = LocalDate.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDate end = LocalDate.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

        // Initialize a variable to track the index of the last assigned engineer
        int lastAssignedIndex = -1;

        // Loop through all the dates between start and end dates
        while (!start.isAfter(end)) {
            // Check if the day matches the specified `stcDay`
            if (start.getDayOfWeek().getValue() == stcDay) {
                // Create a final variable to hold the current date
                final LocalDate currentDate = start;

                // Check if an STC task already exists on this date
                boolean stcTaskExists = taskRepository.findByNameAndCreatedDate("stc", currentDate).size() > 0;

                if (!stcTaskExists) {
                    // Get the next available engineer in a round-robin manner
                    int totalEngineers = engineers.size();
                    Engineer engineerToAssign = null;

                    // Loop through engineers to find the next available one with the STC attribute set to true
                    for (int i = 1; i <= totalEngineers; i++) {
                        int index = (lastAssignedIndex + i) % totalEngineers;
                        Engineer candidate = engineers.get(index);
                        if (candidate != null && Boolean.TRUE.equals(candidate.getStc())) {
                            engineerToAssign = candidate;
                            lastAssignedIndex = index;
                            break;
                        }
                    }

                    // If an engineer is found, assign the STC task
                    if (engineerToAssign != null) {
                        Date date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        saveTask(engineerToAssign.getId(), "stc", date, engineerToAssign.getTeamId());
                    }
                }
            }
            // Move to the next day
            start = start.plusDays(1);
        }
    }
}
