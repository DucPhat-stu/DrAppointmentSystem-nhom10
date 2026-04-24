package com.healthcare.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_records")
public class MedicalRecordEntity {

    @Id
    private UUID id;

    @Column(name = "record_code", nullable = false, unique = true)
    private String recordCode;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String department;

    @Column(name = "disease_summary", nullable = false)
    private String diseaseSummary;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "checkin_time")
    private OffsetDateTime checkinTime;

    @Column(columnDefinition = "TEXT")
    private String tests;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // -- Getters & Setters --

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRecordCode() { return recordCode; }
    public void setRecordCode(String recordCode) { this.recordCode = recordCode; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDiseaseSummary() { return diseaseSummary; }
    public void setDiseaseSummary(String diseaseSummary) { this.diseaseSummary = diseaseSummary; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public OffsetDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(OffsetDateTime checkinTime) { this.checkinTime = checkinTime; }

    public String getTests() { return tests; }
    public void setTests(String tests) { this.tests = tests; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
