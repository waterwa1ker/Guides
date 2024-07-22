package com.example.guides.service;

import com.example.guides.model.Referral;
import com.example.guides.repository.ReferralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferralService {

    private final ReferralRepository referralRepository;

    @Autowired
    public ReferralService(ReferralRepository referralRepository) {
        this.referralRepository = referralRepository;
    }

    @Transactional
    public void save(Referral referral) {
        referralRepository.save(referral);
    }
}
