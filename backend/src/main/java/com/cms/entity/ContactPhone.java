package com.cms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ContactPhone entity — maps to 'contact_phones' table.
 * label: work | home | personal | other
 */
@Entity
@Table(name = "contact_phones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(nullable = false, length = 50)
    private String label;   // work, home, personal, other

    @Column(nullable = false, length = 20)
    private String phone;
}
