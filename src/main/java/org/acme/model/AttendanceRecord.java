package org.acme.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "attendance_date"}))
public class AttendanceRecord extends PanacheEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    public Employee employee;

    @NotNull
    @Column(name = "attendance_date", nullable = false)
    public LocalDate attendanceDate;

    @Column(name = "check_in_time")
    public LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    public LocalDateTime checkOutTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AttendanceStatus status;
}
