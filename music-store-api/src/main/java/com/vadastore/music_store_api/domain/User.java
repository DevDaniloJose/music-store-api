package com.vadastore.music_store_api.domain;

import com.vadastore.music_store_api.enums.DocumentType;
import com.vadastore.music_store_api.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = true)
    private DocumentType documentType;

    @Column(nullable = false, unique = false)
    private String password;

    @ElementCollection(targetClass = Role.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Role> roles;

}
