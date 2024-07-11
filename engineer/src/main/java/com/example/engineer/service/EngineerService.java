package com.example.engineer.service;

import com.example.engineer.DTO.EngineerDTO;
import com.example.engineer.model.Engineer;
import com.example.engineer.repository.EngineerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EngineerService {

    @Autowired
    private EngineerRepository engineerRepository;

    public List<EngineerDTO> getAllEngineers() {
        List<Engineer> engineers = engineerRepository.findAll();
        return engineers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    public List<EngineerDTO> getEngineersByTeamId(Long teamId) {
        List<Engineer> engineers = engineerRepository.findByTeamId(teamId);
        return engineers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    public EngineerDTO getEngineerById(Long id) {
        Engineer engineer = engineerRepository.findEngineerById(id);
        return convertToDTO(engineer);
    }

    public EngineerDTO saveEngineer(EngineerDTO engineerDTO) {
        Engineer engineer = convertToEntity(engineerDTO);
        Engineer savedEngineer = engineerRepository.save(engineer);
        return convertToDTO(savedEngineer);
    }

    public EngineerDTO updateEngineer(Long id, EngineerDTO engineerDTO) {
        Engineer engineer = engineerRepository.findEngineerById(id);
        if (engineer != null) {
            engineer.setName(engineerDTO.getName());
            engineer.setTeamId(engineerDTO.getTeamId());
            Engineer updatedEngineer = engineerRepository.save(engineer);
            return convertToDTO(updatedEngineer);
        } else {
            throw new RuntimeException("Engineer not found with id " + id);
        }
    }

    public void deleteEngineer(Long id) {
        engineerRepository.deleteById(id);
    }

    private EngineerDTO convertToDTO(Engineer engineer) {
        return EngineerDTO.builder()
                .id(engineer.getId())
                .name(engineer.getName())
                .teamId(engineer.getTeamId())
                .build();
    }

    private Engineer convertToEntity(EngineerDTO engineerDTO) {
        return Engineer.builder()
                .id(engineerDTO.getId())
                .name(engineerDTO.getName())
                .teamId(engineerDTO.getTeamId())
                .build();
    }
}