package com.vadastore.music_store_api.domain;

import com.vadastore.music_store_api.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "seller")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "seller")

    private List<Product> products = new ArrayList<>();

    @Column(nullable = false, unique = true)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false, unique = true)
    @Pattern(
            regexp = "^(\\d{11}|\\d{14})$",
            message = "Document number must contain exactly 11 digits (CPF) or 14 digits (CNPJ)"
    )
    private String documentNumber;

    private String postalCode;
    private double rating;
    private boolean isActive;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.rating = 5.0;
    }
}