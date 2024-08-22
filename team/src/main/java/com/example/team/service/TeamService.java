package com.example.team.service;

import com.example.team.DTO.TeamDTO;
import com.example.team.model.Team;
import com.example.team.repository.TeamRepository;
import com.example.team.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private RestTemplate restTemplate;

    public List<TeamDTO> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ResponseBodyVo getTeamById(Long id) {
        //create response body
        ResponseBodyVo responseBodyVo =new ResponseBodyVo();
            //fetch team by id from database
            Team t=teamRepository.findTeamById(id);
            TeamDTO teamDTO=convertToDTO(t);
            responseBodyVo.setTeamDTO(teamDTO);
            //fetch engineers list from engineer microservice
            String UrlEngineer= "http://10.10.30.31:8081/api/engineers/team/" + id ;
            ResponseEntity<List<Engineer>> response = restTemplate.exchange(
                UrlEngineer,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Engineer>>() {}
            );


            List<Engineer>  eList = response.getBody();

            responseBodyVo.setEngineerList(eList);


        return responseBodyVo;
    }

    public TeamDTO saveTeam(TeamDTO teamDTO) {
        Team team = convertToEntity(teamDTO);
        Team savedTeam = teamRepository.save(team);
        return convertToDTO(savedTeam);
    }

    public TeamDTO updateTeam(Long id, TeamDTO teamDTO) {
        Team team = teamRepository.findTeamById(id);
        if (team != null) {
            team.setName(teamDTO.getName());
            Team updatedTeam = teamRepository.save(team);
            return convertToDTO(updatedTeam);
        } else {
            throw new RuntimeException("Team not found with id " + id);
        }
    }

    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }

    private TeamDTO convertToDTO(Team team) {
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .build();
    }

    private Team convertToEntity(TeamDTO teamDTO) {
        return Team.builder()
                .id(teamDTO.getId())
                .name(teamDTO.getName())
                .build();
    }
}