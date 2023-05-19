package com.yilinglin10.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
