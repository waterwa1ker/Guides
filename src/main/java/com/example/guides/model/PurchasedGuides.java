package com.example.guides.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "purchased_guides")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedGuides {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    private Guide guide;

    public PurchasedGuides(Person person, Guide guide) {
        this.person = person;
        this.guide = guide;
    }
}
