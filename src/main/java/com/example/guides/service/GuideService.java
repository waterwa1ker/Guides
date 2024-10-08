package com.example.guides.service;

import com.example.guides.constant.Language;
import com.example.guides.model.Guide;
import com.example.guides.repository.GuideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GuideService {

    private final GuideRepository guideRepository;

    @Autowired
    public GuideService(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    public List<Guide> findTopGuidesByEarnings(Language language) {
        return guideRepository.findTop10ByLanguageOrderByEarningsDesc(language);
    }

    public Optional<Guide> findById(long id) {
        return guideRepository.findById(id);
    }

    @Transactional
    public void save(Guide guide) {
        guide.setCreatedAt(LocalDateTime.now());
        guideRepository.save(guide);
    }
}
