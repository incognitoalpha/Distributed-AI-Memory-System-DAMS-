package com.aimemory.compliance.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@Testcontainers
@ActiveProfiles("test")
class ErasureIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ErasureOrchestrationService erasureService;

    @MockBean
    private JdbcTemplate jdbcTemplate; // Mock DB to avoid needing Postgres for this Kafka test

    @Test
    void initiateErasure_publishesAllRequiredEvents() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        // Setup Kafka consumer to verify events
        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(List.of("audit.event", "compliance.vector-deletion", "compliance.search-deletion"));

        // Act
        erasureService.initiateErasure(tenantId, userId, requestId);

        // Assert - poll for events
        int eventsFound = 0;
        long startTime = System.currentTimeMillis();
        while (eventsFound < 3 && (System.currentTimeMillis() - startTime) < 10000) {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                eventsFound++;
                assertThat(record.value()).contains(userId.toString());
            }
        }

        assertThat(eventsFound).isGreaterThanOrEqualTo(3);
        consumer.close();
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(props);
    }
}
