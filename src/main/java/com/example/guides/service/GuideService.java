package com.example.guides.service;

import com.example.guides.model.Guide;
import com.example.guides.repository.GuideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuideService {

    private final GuideRepository guideRepository;

    @Autowired
    public GuideService(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    @Transactional
    public void save(Guide guide) {
        guideRepository.save(guide);
    }
}
