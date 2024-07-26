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
    @NonNull
    private Boolean chat;
    @NonNull
    private Boolean p1;
    @NonNull
    private Boolean qm;
    @NonNull
    private Boolean stc;

}
