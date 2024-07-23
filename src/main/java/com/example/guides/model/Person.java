package com.example.guides.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {

    @Id
    private long id;

    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String description;

    private String role;

    private String password;

    @Column(name = "referral_link")
    private String referralLink;

    @OneToMany(mappedBy = "referral")
    private List<Referral> referrals;

    @OneToMany(mappedBy = "referralOwner")
    private List<Referral> referralOwners;

    @OneToMany(mappedBy = "author")
    private List<Guide> guides;

    @OneToMany(mappedBy = "person")
    private List<PurchasedGuides> purchasedGuides;

}
