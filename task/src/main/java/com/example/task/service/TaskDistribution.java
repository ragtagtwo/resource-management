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
        startDate.set(2024, Calendar.AUGUST, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2024, Calendar.AUGUST, 31);

        distributeP1Tasks(engineers, startDate, endDate);
        distributeChatTasks(engineers, startDate, endDate);
    }

    public void distributeP1Tasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        int engineerCount = engineers.size();
        int startMonth = startDate.get(Calendar.MONTH);
        int year = startDate.get(Calendar.YEAR);
        Calendar calendar = (Calendar) startDate.clone();
        int startIndex = 0;

        for (int week = 0; week < 4; week++) {
            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                calendar.set(Calendar.MONTH, startMonth);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.WEEK_OF_MONTH, week + 1);
                calendar.set(Calendar.DAY_OF_WEEK, day);
                Date currentDate = calendar.getTime();

                boolean taskAssigned = false;
                for (int i = 0; i < engineerCount; i++) {
                    int engineerIndex = (startIndex + i) % engineerCount;
                    Engineer engineer = engineers.get(engineerIndex);

                    if (Boolean.TRUE.equals(engineer.getP1()) && checkAvailable(engineer, currentDate)) {
                        saveTask(engineer.getId(), "P1", currentDate);
                        taskAssigned = true;
                        startIndex = (engineerIndex + 1) % engineerCount;
                        break;
                    }
                }

                if (!taskAssigned) {
                    System.out.println("No available engineer for P1 task on " + currentDate);
                }
            }
            // Increment startIndex for the next week
            startIndex = (startIndex + 1) % engineerCount;
        }
    }

    public void distributeChatTasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        int engineerCount = engineers.size();

        for (Calendar date = (Calendar) startDate.clone(); !date.after(endDate); date.add(Calendar.DATE, 1)) {
            if (isWeekend(date)) continue; // Skip weekends

            int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);

            for (int shift = 0; shift < 2; shift++) { // 0 for morning, 1 for afternoon
                int engineersAssigned = 0;

                for (int j = 0; j < engineerCount && engineersAssigned < 2; j++) {
                    Engineer engineer = engineers.get((dayOfMonth + j + shift * 2) % engineerCount);

                    if (Boolean.TRUE.equals(engineer.getChat()) && checkAvailable(engineer, date.getTime())) {
                        saveChat(engineer.getId(), "chat", date.getTime(), shift == 0 ? "morning" : "afternoon");
                        engineersAssigned++;
                    }
                }
            }
        }
    }

    private boolean checkAvailable(Engineer engineer, Date date) {
        // Check if engineer is on vacation
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Vacation> vacations = vacationRepository.findByEngineerIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                engineer.getId(), localDate, localDate);
        if (!vacations.isEmpty()) {
            return false;
        }

        // Check if engineer has other tasks on the same day
        List<Task> tasks = taskRepository.findByEngineerIdAndCreatedDate(engineer.getId(), localDate);
        return tasks.isEmpty();
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