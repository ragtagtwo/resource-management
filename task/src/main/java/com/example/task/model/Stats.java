package com.example.task.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="Stats")
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Assuming there's a primary key field
    private int engineerId;
    private int teamID;
    private int p1;
    private int stc;
    private int chat;
    private int qm;
    private int score;
    private LocalDate startDate;
    private LocalDate endDate;
}