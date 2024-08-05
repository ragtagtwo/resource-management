package com.example.task.DTO;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VacationDTO {
    private Long id;
    private Long engineerId;
    private LocalDate startDate;
    private Long teamId;
    private LocalDate endDate;
    private String shift;
}
