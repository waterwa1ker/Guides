package com.example.guides.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "referrals")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "referral_owner_id")
    private Person referralOwner;

    @ManyToOne
    @JoinColumn(name = "referral_id")
    private Person referral;

    public Referral(Person referralOwner, Person referral) {
        this.referralOwner = referralOwner;
        this.referral = referral;
    }
}
