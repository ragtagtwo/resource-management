package com.example.task.controller;

import com.example.task.DTO.TaskDTO;
import com.example.task.service.TaskDistribution;
import com.example.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = {"http://localhost:4200", "http://10.10.30.31:4200"})
@RestController
@RequestMapping("/api")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskDistribution taskDistribution;

    @PostMapping("/distribute")
    public void distributeTasks(
            @RequestParam int number,
            @RequestParam Long teamId,
            @RequestParam(required = false) Integer stc) {
        if (number == 1) {
            taskDistribution.distributeAll(teamId, stc);
        } else if (number == 2) {
            taskDistribution.reset(teamId);
        }
    }
    @GetMapping("/tasks/team/{teamId}")
    public List<TaskDTO> getTasksByTeamId(@PathVariable Long teamId) {
        return taskService.getTasksByTeamId(teamId);
    }

    @GetMapping("/tasks")
    public List<TaskDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/tasks/{id}")
    public TaskDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping("/tasks")
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO) {
        return taskService.saveTask(taskDTO);
    }

    @PutMapping("/tasks/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        return taskService.updateTask(id, taskDTO);
    }

    @DeleteMapping("/tasks/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/tasks/engineer/{engineerId}")
    public List<TaskDTO> getTasksByEngineerId(@PathVariable Long engineerId) {
        return taskService.getTasksByEngineerId(engineerId);
    }
    // New API endpoint to call the equilibrate function
    @PostMapping("/equilibrate")
    public void equilibrateTasks(@RequestParam int type, @RequestParam Long teamId) {
        taskDistribution.equilibrate(type, teamId);
    }

}
