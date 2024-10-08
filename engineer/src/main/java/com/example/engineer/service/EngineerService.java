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
            engineer.setChat(engineerDTO.getChat());
            engineer.setP1(engineerDTO.getP1());
            engineer.setQm(engineerDTO.getQm());
            engineer.setStc(engineerDTO.getStc());
            Engineer updatedEngineer = engineerRepository.save(engineer);
            return convertToDTO(updatedEngineer);
        } else {
            throw new RuntimeException("Engineer not found with id " + id);
        }
    }

    public EngineerDTO deleteEngineer(Long id) {
        Engineer engineer = engineerRepository.findEngineerById(id);
        EngineerDTO engineerDTO = convertToDTO(engineer);
        engineerRepository.deleteById(id);
        return engineerDTO;
    }

    private EngineerDTO convertToDTO(Engineer engineer) {
        return EngineerDTO.builder()
                .id(engineer.getId())
                .name(engineer.getName())
                .teamId(engineer.getTeamId())
                .chat(engineer.getChat())
                .p1(engineer.getP1())
                .qm(engineer.getQm())
                .stc(engineer.getStc())
                .build();
    }

    private Engineer convertToEntity(EngineerDTO engineerDTO) {
        return Engineer.builder()
                .id(engineerDTO.getId())
                .name(engineerDTO.getName())
                .teamId(engineerDTO.getTeamId())
                .chat(engineerDTO.getChat())
                .p1(engineerDTO.getP1())
                .qm(engineerDTO.getQm())
                .stc(engineerDTO.getStc())
                .build();
    }
}