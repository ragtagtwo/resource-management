package com.example.task.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="vacations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vacation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long engineerId;
    private Long teamId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String shift;
}