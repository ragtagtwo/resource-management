package com.example.team.vo;

import com.example.team.DTO.TeamDTO;
import lombok.*;

import java.util.List;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBodyVo {
    private TeamDTO teamDTO;
    private List<Engineer> engineerList;
}
