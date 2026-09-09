package org.acme.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "employee")
public class Employee extends PanacheEntity {

    @NotBlank
    public String name;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    public String email;

    public String department;
}
