package com.example.task.DTO;

import lombok.*;

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
    private LocalDateTime createdDate;
    private String shift;
}
