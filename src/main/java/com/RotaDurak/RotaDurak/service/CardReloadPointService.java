package com.RotaDurak.RotaDurak.service;
import com.RotaDurak.RotaDurak.model.CardReloadPoint;
import com.RotaDurak.RotaDurak.repository.CardReloadPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardReloadPointService {

    @Autowired
    private CardReloadPointRepository cardReloadPointRepository;

    public List<CardReloadPoint> getAllReloadPoints() {
        return cardReloadPointRepository.findAll();
    }

    public Optional<CardReloadPoint> getReloadPointById(Long id) {
        return cardReloadPointRepository.findById(id);
    }

    public CardReloadPoint createReloadPoint(CardReloadPoint reloadPoint) {
        return cardReloadPointRepository.save(reloadPoint);
    }
}
