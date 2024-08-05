package com.example.task.VO;

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
    private int priority;
    private int p1Done;
    private int index;
}