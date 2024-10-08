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
    private Boolean chat;
    private Boolean p1;
    private Boolean qm;
    private Boolean stc;
}
