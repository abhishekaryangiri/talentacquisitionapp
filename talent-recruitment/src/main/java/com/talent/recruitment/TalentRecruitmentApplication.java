package com.talent.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.talent.recruitment")
public class TalentRecruitmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(TalentRecruitmentApplication.class, args);
	}

}
