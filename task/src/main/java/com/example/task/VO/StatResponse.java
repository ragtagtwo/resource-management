package com.example.task.VO;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StatResponse {
    private int engineerId;
    private int teamID;
    private int p1;
    private  int stc;
    private int chat;
    private int qm;
    private int score;

}
