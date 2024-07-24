package com.example.engineer.DTO;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EngineerDTO {
    private Long id;
    private String name;
    private Long teamId;
    private Boolean chat;
    private Boolean p1;
    private Boolean qm;
    private Boolean stc;

}
