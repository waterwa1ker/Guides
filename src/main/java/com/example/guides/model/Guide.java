package com.example.guides.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Table(name = "guide")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "main_img")
    private String mainImg;

    private String description;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person author;

    @OneToMany(mappedBy = "guide")
    private List<Chapter> chapters;

}
