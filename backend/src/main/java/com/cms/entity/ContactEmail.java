package com.cms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ContactEmail entity — maps to 'contact_emails' table.
 * label: work | personal | other
 */
@Entity
@Table(name = "contact_emails")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(nullable = false, length = 50)
    private String label;    // work, personal, other

    @Column(nullable = false, length = 255)
    private String email;
}
