package com.vadastore.music_store_api.domain;


import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;



    @Column(nullable = false, unique = false)
    private String street;

    @Column(nullable = false, unique = false)
    private String city;

    @Column(nullable = false, unique = false)
    private String state;

    @Column(nullable = false, unique = false)
    private String zipCode;

    @Column(nullable = false, unique = false)
    private boolean isDefault;

}
