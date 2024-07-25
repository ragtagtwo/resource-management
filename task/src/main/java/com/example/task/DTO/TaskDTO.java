package com.example.task.DTO;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDTO {
    private Long id;
    private String name;
    private Long engineerId;
    private LocalDate createdDate;
    private String shift;
}
