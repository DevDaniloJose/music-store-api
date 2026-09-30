package com.vadastore.music_store_api;

import com.vadastore.music_store_api.security.JwtUtility;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class MusicStoreApiApplication implements CommandLineRunner {

	@Autowired
	private JwtUtility jwtUtility;

	public static void main(String[] args) {
		SpringApplication.run(MusicStoreApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Map<String, String> metadata = new HashMap<>();

		metadata.put("id", "1L");
		metadata.put("role", "USER");
		metadata.put("email", "werewolfasso@gmail.com");

		int validityInterval = 24 * 60 * 60 * 1000;

		String token = jwtUtility.generateToken(metadata, "masayoshi", validityInterval);

		System.out.println("JWT Token: " + token);

		Claims claims = jwtUtility.extractAllClaims(token);

		System.out.println("Subject: "+ claims.getSubject());
		System.out.println("Extras: "+ claims.keySet());


	}
}
