package medplan.controller;

import medplan.entity.User;
import medplan.repository.UserRepository;
import medplan.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record RegisterRequest(
        String fullName,
        String email,
        String password
    ) {}

    public record LoginRequest(
        String email,
        String password
    ) {}

    public record AuthResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String token
    ) {}

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Email already exists"
            );
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.PATIENT);

        User saved = userRepository.save(user);

        return new AuthResponse(
            saved.getId(),
            saved.getFullName(),
            saved.getEmail(),
            saved.getRole().name(),
            jwtService.generateToken(saved.getEmail())
        );
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid email or password"
            ));

        if (!passwordEncoder.matches(
                request.password(), user.getPassword())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid email or password"
            );
        }

        return new AuthResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().name(),
            jwtService.generateToken(user.getEmail())
        );
    }
}





