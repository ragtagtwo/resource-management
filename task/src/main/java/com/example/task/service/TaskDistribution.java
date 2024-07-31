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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class TaskDistribution {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private VacationRepository vacationRepository;

    public void distributeAll() {
        RestTemplate restTemplate = new RestTemplate();
        String engineersUrl = "http://localhost:8081/api/engineers";
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
    public void reset() {
        LocalDate today = LocalDate.now();
        taskRepository.deleteByCreatedDateGreaterThanEqualAndNameIn(today, List.of("p1", "chat"));
    }

    public void distributeP1Tasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        int engineerCount = engineers.size();
        Calendar calendar = (Calendar) startDate.clone();
        int startIndex = 0;

        while (calendar.before(endDate)) {
            if (!isWeekend(calendar)) {
                Date currentDate = calendar.getTime();

                boolean taskAssigned = false;
                for (int i = 0; i < engineerCount; i++) {
                    int engineerIndex = (startIndex + i) % engineerCount;
                    Engineer engineer = engineers.get(engineerIndex);

                    if (Boolean.TRUE.equals(engineer.getP1()) && checkAvailableForP1(engineer, currentDate) && checkAvailabilityP1(currentDate)) {
                        saveTask(engineer.getId(), "p1", currentDate);
                        taskAssigned = true;
                        startIndex = (engineerIndex + 1) % engineerCount;
                        break;
                    }
                }

            }
            calendar.add(Calendar.DATE, 1);
        }
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
                            saveChat(engineer.getId(), "chat", date.getTime(), shiftName);
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

        // Check if the engineer has other chat tasks during this shift
        List<Task> tasks = taskRepository.findByEngineerIdAndCreatedDateAndShift(engineer.getId(), localDate, shift);
        return tasks.isEmpty();
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

    private void saveChat(Long engineerId, String taskName, Date date, String shift) {
        Task task = Task.builder()
                .name(taskName)
                .engineerId(engineerId)
                .createdDate(LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                .shift(shift)
                .build();
        taskRepository.save(task);
    }

    private void saveTask(Long engineerId, String taskName, Date date) {
        Task task = Task.builder()
                .name(taskName)
                .engineerId(engineerId)
                .createdDate(LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                .build();
        taskRepository.save(task);
    }

    private boolean isWeekend(Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
    }
}