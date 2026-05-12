package com.aimemory.ranking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ranking Engine - scores memory importance using heuristic model.
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication
public class RankingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RankingEngineApplication.class, args);
    }
}