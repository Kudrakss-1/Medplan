package medplan.controller;

import medplan.entity.Specialty;
import medplan.repository.SpecialtyRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyController(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @GetMapping
    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    @PostMapping
    public Specialty createSpecialty(@RequestBody Specialty specialty) {
        return specialtyRepository.save(specialty);
    }
}