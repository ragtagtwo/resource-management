package com.example.task.service;

import com.example.task.DTO.TaskDTO;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findTaskById(id);
        return convertToDTO(task);
    }

    public TaskDTO saveTask(TaskDTO taskDTO) {
        Task task = convertToEntity(taskDTO);
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findTaskById(id);
        if (task != null) {
            task.setName(taskDTO.getName());
            task.setEngineerId(taskDTO.getEngineerId());
            Task updatedTask = taskRepository.save(task);
            return convertToDTO(updatedTask);
        } else {
            throw new RuntimeException("Task not found with id " + id);
        }
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<TaskDTO> getTasksByEngineerId(Long engineerId) {
        List<Task> tasks = taskRepository.findByEngineerId(engineerId);
        return tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private TaskDTO convertToDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .engineerId(task.getEngineerId())
                .build();
    }

    private Task convertToEntity(TaskDTO taskDTO) {
        return Task.builder()
                .id(taskDTO.getId())
                .name(taskDTO.getName())
                .engineerId(taskDTO.getEngineerId())
                .build();
    }
}
