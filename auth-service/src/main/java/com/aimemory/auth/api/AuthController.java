package com.aimemory.auth.api;

import com.aimemory.auth.api.dto.RefreshTokenRequest;
import com.aimemory.auth.api.dto.TokenRequest;
import com.aimemory.auth.api.dto.TokenResponse;
import com.aimemory.auth.service.TokenService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for authentication operations.
 *
 * @author agent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Issues a new JWT token for the given tenant and user.
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> createToken(
            @Valid @RequestBody TokenRequest request) {

        log.info("Issuing token for tenantId={} userId={}", request.tenantId(), request.userId());

        List<String> roles = request.roles() != null ? request.roles() : List.of("ROLE_USER");

        String token = tokenService.generateToken(
                request.tenantId(),
                request.userId(),
                roles
        );

        TokenResponse response = new TokenResponse(
                token,
                "Bearer",
                3600, // Should match config
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Refreshes an existing JWT token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Refreshing token");

        String newToken = tokenService.refreshToken(request.token());

        TokenResponse response = new TokenResponse(
                newToken,
                "Bearer",
                3600,
                Instant.now()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}