package org.example.authservice;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
	@GetMapping("/auth-service/health")
	public Map<String, Object> health() {
		return Map.of("status", "UP");
	}
}