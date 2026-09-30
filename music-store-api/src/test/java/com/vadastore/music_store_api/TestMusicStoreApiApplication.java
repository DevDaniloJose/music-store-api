package com.vadastore.music_store_api;

import org.springframework.boot.SpringApplication;

public class TestMusicStoreApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(MusicStoreApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
