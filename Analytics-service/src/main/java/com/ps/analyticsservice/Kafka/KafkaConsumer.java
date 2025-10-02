package com.ps.analyticsservice.Kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.event.PatientEvent;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics ="patient", groupId = "analytics-service")
public void consumeEvent(byte [] event){

    try {
        PatientEvent patientEvent = PatientEvent.parseFrom(event);
        log.error("patient event received is {}", patientEvent.getName());
    } catch (InvalidProtocolBufferException e) {
        log.error("Error while parsing event", e);
        throw new RuntimeException(e);
    }

}

}
