package com.example.task.controller;

import com.example.task.DTO.VacationDTO;
import com.example.task.service.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/vacations")
public class VacationRestController {

    @Autowired
    private VacationService vacationService;

    @GetMapping
    public ResponseEntity<List<VacationDTO>> getAllVacations() {
        List<VacationDTO> vacations = vacationService.getAllVacations();
        return ResponseEntity.ok(vacations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacationDTO> getVacationById(@PathVariable Long id) {
        VacationDTO vacation = vacationService.getVacationById(id);
        return ResponseEntity.ok(vacation);
    }

    @PostMapping
    public ResponseEntity<VacationDTO> createVacation(@RequestBody VacationDTO vacationDTO) {
        VacationDTO savedVacation = vacationService.saveVacation(vacationDTO);
        return ResponseEntity.ok(savedVacation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VacationDTO> updateVacation(@PathVariable Long id, @RequestBody VacationDTO vacationDTO) {
        VacationDTO updatedVacation = vacationService.updateVacation(id, vacationDTO);
        return ResponseEntity.ok(updatedVacation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacation(@PathVariable Long id) {
        vacationService.deleteVacation(id);
        return ResponseEntity.noContent().build();
    }
}