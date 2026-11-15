package medplan.controller;

import medplan.entity.Appointment;
import medplan.entity.Doctor;
import medplan.entity.User;
import medplan.repository.AppointmentRepository;
import medplan.repository.DoctorRepository;
import medplan.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public AppointmentController(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    public record CreateAppointmentRequest(
        Long patientId,
        Long doctorId,
        LocalDateTime appointmentDate
    ) {}

    public record AppointmentResponse(
        Long id,
        Long patientId,
        Long doctorId,
        LocalDateTime appointmentDate,
        String status
    ) {}

    @PostMapping
    public AppointmentResponse createAppointment(
            @RequestBody CreateAppointmentRequest request) {

        User patient = userRepository.findById(request.patientId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Patient not found"));

        Doctor doctor = doctorRepository.findById(request.doctorId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Doctor not found"));

        if (request.appointmentDate() == null ||
                !request.appointmentDate().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid appointment date");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStatus(Appointment.Status.PENDING);

        return toResponse(appointmentRepository.save(appointment));
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponse> getPatientAppointments(
            @PathVariable Long patientId) {

        return appointmentRepository
            .findByPatient_IdOrderByAppointmentDateDesc(patientId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancelAppointment(
            @PathVariable Long id) {

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Appointment already completed");
        }

        appointment.setStatus(Appointment.Status.CANCELLED);

        return toResponse(appointmentRepository.save(appointment));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getPatient().getId(),
            appointment.getDoctor().getId(),
            appointment.getAppointmentDate(),
            appointment.getStatus().name()
        );
    }
}