package com.example.task.service;

import com.example.task.VO.Engineer;
import com.example.task.model.Task;
import com.example.task.model.Vacation;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.VacationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskDistribution {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private VacationRepository vacationRepository;

    public void distributeChatTasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        int engineerCount = engineers.size();

        for (Calendar date = (Calendar) startDate.clone(); !date.after(endDate); date.add(Calendar.DATE, 1)) {
            if (isWeekend(date)) continue; // Skip weekends

            int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);

            for (int shift = 0; shift < 2; shift++) { // 0 for morning, 1 for afternoon
                int engineersAssigned = 0;

                for (int j = 0; j < engineerCount && engineersAssigned < 2; j++) {
                    Engineer engineer = engineers.get((dayOfMonth + j + shift * 2) % engineerCount);

                    if (engineer.getChat() && checkAvailable(engineer, date.getTime())) {
                        saveChat(engineer.getId(), "chat", date.getTime(), shift == 0 ? "morning" : "afternoon");
                        engineersAssigned++;
                    }
                }
            }
        }
    }

    public void distributeP1Tasks(List<Engineer> engineers, Calendar startDate, Calendar endDate) {
        int engineerCount = engineers.size();
        int startingEngineerIndex = 0;

        for (Calendar date = (Calendar) startDate.clone(); !date.after(endDate); date.add(Calendar.DATE, 1)) {
            if (isWeekend(date)) continue; // Skip weekends

            Engineer engineer = null;
            for (int i = 0; i < engineerCount; i++) {
                engineer = engineers.get((startingEngineerIndex + i) % engineerCount);
                if (engineer.getP1() && checkAvailable(engineer, date.getTime())) {
                    break;
                }
            }

            if (engineer != null) {
                saveTask(engineer.getId(), "p1", date.getTime());
                startingEngineerIndex = (startingEngineerIndex + 1) % engineerCount; // Move to the next engineer for the next day
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
                .createdDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                .shift(shift)
                .build();
        taskRepository.save(task);
    }

    private void saveTask(Long engineerId, String taskName, Date date) {
        Task task = Task.builder()
                .name(taskName)
                .engineerId(engineerId)
                .createdDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                .build();
        taskRepository.save(task);
    }

    private boolean isWeekend(Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
    }
}
