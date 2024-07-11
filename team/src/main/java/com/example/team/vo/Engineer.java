package com.example.team.vo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Engineer {
    private Long id;

    private String name;

    private Long teamId;
}
