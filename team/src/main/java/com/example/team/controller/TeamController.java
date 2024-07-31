package com.example.team.controller;

import com.example.team.DTO.TeamDTO;

import com.example.team.service.TeamService;
import com.example.team.vo.ResponseBodyVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamController {
    @Autowired
    private TeamService teamService;
    @GetMapping("/teams")
    public List<TeamDTO> getAllTeams() {
        return teamService.getAllTeams();
    }
    @GetMapping("/teams/{id}")
    public ResponseBodyVo getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id);
    }
    @PostMapping("/teams")
    public TeamDTO createTeam(@RequestBody TeamDTO teamDTO) {
        return teamService.saveTeam(teamDTO);
    }

    @PutMapping("/teams/{id}")
    public TeamDTO updateTeam(@PathVariable Long id, @RequestBody TeamDTO teamDTO) {
        return teamService.updateTeam(id, teamDTO);
    }

    @DeleteMapping("/teams/{id}")
    public void deleteEngineer(@PathVariable Long id) {
        teamService.deleteTeam(id);
    }

}