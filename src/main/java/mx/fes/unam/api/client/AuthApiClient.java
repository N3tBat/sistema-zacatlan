package mx.fes.unam.api.client;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.dto.auth.LoginRequest;
import mx.fes.unam.api.dto.auth.LoginResponse;
import mx.fes.unam.api.dto.auth.MeResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AuthApiClient {

    private final RestClient restClient;

    public LoginResponse login(LoginRequest request) {
        return restClient.post()
                .uri("/api/auth/login")
                .body(request)
                .retrieve()
                .body(LoginResponse.class);
    }

    public MeResponse me() {
        return restClient.get()
                .uri("/api/auth/me")
                .retrieve()
                .body(MeResponse.class);
    }
}
