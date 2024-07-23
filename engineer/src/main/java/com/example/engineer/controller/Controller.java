package com.example.engineer.controller;

import com.example.engineer.DTO.EngineerDTO;
import com.example.engineer.service.EngineerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class Controller {
    @Autowired
    private EngineerService engineerService;

    @GetMapping("/engineers")
    public List<EngineerDTO> getAllEngineers() {
        return engineerService.getAllEngineers();
    }

    @GetMapping("/engineers/{id}")
    public EngineerDTO getEngineerById(@PathVariable Long id) {
        return engineerService.getEngineerById(id);
    }
    @GetMapping("/engineers/team/{teamId}")
    public List<EngineerDTO> getEngineersByTeamId(@PathVariable Long teamId) {
        return engineerService.getEngineersByTeamId(teamId);
    }
    @PostMapping("/engineers")
    public EngineerDTO createEngineer(@RequestBody EngineerDTO engineerDTO) {
        return engineerService.saveEngineer(engineerDTO);
    }

    @PutMapping("/engineers/{id}")
    public EngineerDTO updateEngineer(@PathVariable Long id, @RequestBody EngineerDTO engineerDTO) {
        return engineerService.updateEngineer(id, engineerDTO);
    }

    @DeleteMapping("/engineers/{id}")
    public EngineerDTO deleteEngineer(@PathVariable Long id) {
        return engineerService.deleteEngineer(id);
    }
}
