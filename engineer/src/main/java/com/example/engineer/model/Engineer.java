package com.example.engineer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="engineers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Engineer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long teamId;

    private Boolean chat;

    private Boolean p1;

    private Boolean qm;

    private Boolean stc;

}
