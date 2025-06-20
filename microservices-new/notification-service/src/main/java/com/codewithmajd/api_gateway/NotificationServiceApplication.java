package com.codewithmajd.api_gateway;

import lombok.extern.slf4j.Slf4j;
import com.codewithmajd.api_gateway.event.OrderPlacedEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
@Slf4j
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @KafkaListener(topics = "notificationTopic")
    public void handleNotifications(OrderPlacedEvent orderPlacedEvent){
        log.info("Notification was sent successfully: {}", orderPlacedEvent.getOrderNumber());
    }
}
