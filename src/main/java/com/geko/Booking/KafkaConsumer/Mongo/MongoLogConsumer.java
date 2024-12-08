package com.geko.Booking.KafkaConsumer.Mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.Entity.Mongo.AuditLog;
import com.geko.Booking.Repository.Mongodb.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static com.geko.Booking.Entity.Mongo.Action.*;

@Service
public class MongoLogConsumer {
    private static final String TOPIC = "log-topic";
    private static final String GROUP = "booking-mongo-consumer-group";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PATH = "C:\\Users\\gekol\\Desktop\\Booking\\logs.txt";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final BlockingQueue<AuditLog> bq = new LinkedBlockingQueue<>();
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public MongoLogConsumer(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        new Thread(this::writeMessagesToFile).start();
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP)
    public void listen(String auditLogJson) {
        try {
            AuditLog auditLog = objectMapper.readValue(auditLogJson, AuditLog.class);
            bq.add(auditLog);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMessagesToFile() {
        while (true) {
            try {
                AuditLog log = bq.take();
                auditLogRepository.save(log);
                String formattedLog = formatMessage(log);
                try (FileWriter writer = new FileWriter(PATH, true)) {
                    writer.write(formattedLog + "\n");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatMessage(AuditLog log) {
        String date = dateFormat.format(new Date());
        String result = date + " --- " + log.getAction() + " - User: " + log.getUsername();

        if (log.getListingId() != null) {
             result += " - Listing id: " + log.getListingId();
        }

        return result;
    }
}
