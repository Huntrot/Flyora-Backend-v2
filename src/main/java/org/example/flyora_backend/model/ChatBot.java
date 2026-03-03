package org.example.flyora_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ChatBot")
public class ChatBot {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Lob
    @Column(name = "message")
    private String message;

    @Lob
    @Column(name = "response")
    private String response;

    @Column(name = "created_at")
    private Instant createdAt;

}