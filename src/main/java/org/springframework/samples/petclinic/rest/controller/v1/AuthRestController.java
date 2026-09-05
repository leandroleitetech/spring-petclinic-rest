package org.springframework.samples.petclinic.rest.controller.v1;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.PasswordChangeRequestDto;
import org.springframework.samples.petclinic.rest.dto.PasswordResetConfirmDto;
import org.springframework.samples.petclinic.rest.dto.PasswordResetRequestDto;
import org.springframework.samples.petclinic.rest.dto.PasswordResetTokenDto;
import org.springframework.samples.petclinic.service.PasswordResetService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api/auth")
public class AuthRestController {

    private final PasswordResetService passwordResetService;

    public AuthRestController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequestDto request, Principal principal) {
        try {
            passwordResetService.changePassword(principal.getName(), request.getCurrentPassword(),
                request.getNewPassword(), request.getConfirmPassword());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<PasswordResetTokenDto> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        String token = passwordResetService.requestPasswordReset(request.getUsername());
        return ResponseEntity.ok(new PasswordResetTokenDto(token));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmDto request) {
        try {
            passwordResetService.confirmPasswordReset(request.getToken(), request.getNewPassword(),
                request.getConfirmPassword());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    private ResponseEntity<ProblemDetail> badRequest(String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setDetail(detail);
        return ResponseEntity.badRequest().body(problem);
    }
}
