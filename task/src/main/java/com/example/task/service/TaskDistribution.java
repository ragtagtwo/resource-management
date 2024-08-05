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

@Service
public class TaskDistribution {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private VacationRepository vacationRepository;

    public void distributeAll(Long teamId) {
        RestTemplate restTemplate = new RestTemplate();
        String engineersUrl = "http://localhost:8081/api/engineers/team/" + teamId;
        ResponseEntity<List<Engineer>> response = restTemplate.exchange(
                engineersUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Engineer>>() {}
        );

        List<Engineer> engineers = response.getBody();

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 3);

        distributeP1Tasks(engineers, startDate, endDate);
        distributeChatTasks(engineers, startDate, endDate);
    }

    @Transactional
    public void reset(Long teamId) {
        LocalDate today = LocalDate.now();
        taskRepository.deleteByCreatedDateGreaterThanEqualAndNameInAndTeamId(today, List.of("p1", "chat"), teamId);
    }
    public void distributeP1Tasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        LocalDate currentDate = LocalDate.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDate endLocalDate = LocalDate.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

        for (int i = 0; i < engineers.size(); i++) {
            engineers.get(i).setIndex(i);
        }

        while (!currentDate.isAfter(endLocalDate)) {
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                currentDate = currentDate.plusDays(1);
                continue; // Skip weekends
            }

            System.out.println("Date: " + currentDate);
            System.out.println("Engineers: " + engineers);

            // Convert LocalDate to Date for checkAvailabilityP1 method
            Date currentDateAsDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (!checkAvailabilityP1(currentDateAsDate)) {
                System.out.println("P1 task already assigned on " + currentDate);
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // Get the first available engineer with priority 0 and p1 attribute true
            Engineer engineerToAssign = engineers.stream()
                    .filter(engineer -> engineer.getPriority() == 0 && engineer.getP1() && checkAvailableForP1(engineer, currentDateAsDate))
                    .findFirst()
                    .orElse(null);

            if (engineerToAssign == null) {
                // If no engineer with priority 0 is available, try those with priority 1 and p1 attribute true
                engineerToAssign = engineers.stream()
                        .filter(engineer -> engineer.getPriority() == 1 && engineer.getP1() && checkAvailableForP1(engineer, currentDateAsDate))
                        .findFirst()
                        .orElse(null);
            }

            if (engineerToAssign != null) {
                // Assign P1 to this engineer
                saveTask(engineerToAssign.getId(), "P1", currentDateAsDate, engineerToAssign.getTeamId());
                engineerToAssign.setPriority(1);
                engineerToAssign.setP1Done(engineerToAssign.getP1Done() + 1);

                // Check if all available engineers with p1 attribute as true have priority 1
                boolean allPrioritiesOne = engineers.stream()
                        .filter(engineer -> engineer.getP1() && checkAvailableForP1(engineer, currentDateAsDate))
                        .allMatch(e -> e.getPriority() == 1);

                if (allPrioritiesOne) {
                    // Reset all priorities for engineers with p1 attribute as true
                    for (Engineer engineer : engineers) {
                        if (engineer.getP1()) {
                            engineer.setPriority(0);
                        }
                    }
                    // Reorder indexes
                    reorderIndexes(engineers);
                }
            } else {
                System.out.println("No available engineer for P1 on " + currentDate);
            }

            currentDate = currentDate.plusDays(1);

            System.out.println("After assignment:");
            System.out.println("Engineers: " + engineers);
            System.out.println("---------------------------------");
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

                        if (Boolean.TRUE.equals(engineer.getChat()) && checkAvailableForChatShift(engineer, date.getTime(), shiftName) && checkAvailabilityChat(date.getTime(), shiftName)) {
                            saveChat(engineer.getId(), "chat", date.getTime(), shiftName, engineer.getTeamId());
                            engineersAssigned++;
                        }
                    }
                }
            }
            date.add(Calendar.DATE, 1);
        }
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


    private boolean checkAvailabilityP1(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Task> tasks = taskRepository.findByNameAndCreatedDate("p1", localDate);
        return tasks.isEmpty();
    }

    private boolean checkAvailabilityChat(Date date, String shift) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Task> tasks = taskRepository.findByNameAndCreatedDateAndShift("chat", localDate, shift);
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
}
