package com.example.team.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="teams")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

}
