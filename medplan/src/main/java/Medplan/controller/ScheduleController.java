package medplan.controller;

import medplan.entity.Schedule;
import medplan.repository.ScheduleRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    public ScheduleController(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Schedule> getDoctorSchedule(@PathVariable Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    @PostMapping
    public Schedule createSchedule(@RequestBody Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}