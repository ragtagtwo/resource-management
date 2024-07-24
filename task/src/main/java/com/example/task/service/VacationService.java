package com.example.task.service;

import com.example.task.DTO.VacationDTO;
import com.example.task.model.Vacation;
import com.example.task.repository.VacationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VacationService {

    @Autowired
    private VacationRepository vacationRepository;

    public List<VacationDTO> getAllVacations() {
        List<Vacation> vacations = vacationRepository.findAll();
        return vacations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public VacationDTO getVacationById(Long id) {
        Vacation vacation = vacationRepository.findById(id).orElse(null);
        return convertToDTO(vacation);
    }

    public VacationDTO saveVacation(VacationDTO vacationDTO) {
        Vacation vacation = convertToEntity(vacationDTO);
        Vacation savedVacation = vacationRepository.save(vacation);
        return convertToDTO(savedVacation);
    }

    public VacationDTO updateVacation(Long id, VacationDTO vacationDTO) {
        Vacation vacation = vacationRepository.findById(id).orElse(null);
        if (vacation != null) {
            vacation.setStartDate(vacationDTO.getStartDate());
            vacation.setEndDate(vacationDTO.getEndDate());
            vacation.setEngineerId(vacationDTO.getEngineerId());
            Vacation updatedVacation = vacationRepository.save(vacation);
            return convertToDTO(updatedVacation);
        } else {
            throw new RuntimeException("Vacation not found with id " + id);
        }
    }

    public void deleteVacation(Long id) {
        vacationRepository.deleteById(id);
    }

    private VacationDTO convertToDTO(Vacation vacation) {
        return VacationDTO.builder()
                .id(vacation.getId())
                .engineerId(vacation.getEngineerId())
                .startDate(vacation.getStartDate())
                .endDate(vacation.getEndDate())
                .build();
    }

    private Vacation convertToEntity(VacationDTO vacationDTO) {
        return Vacation.builder()
                .id(vacationDTO.getId())
                .engineerId(vacationDTO.getEngineerId())
                .startDate(vacationDTO.getStartDate())
                .endDate(vacationDTO.getEndDate())
                .build();
    }
}