package com.example.guides.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "guide")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Guide implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(name = "main_img")
    private String mainImg;

    private String description;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person author;

    @OneToMany(mappedBy = "guide")
    private List<Chapter> chapters;

    private int price;

    private int count;

    private int earnings;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
