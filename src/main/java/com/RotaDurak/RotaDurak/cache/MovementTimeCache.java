package com.RotaDurak.RotaDurak.cache;

import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.repository.MovementTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MovementTimeCache {
    private final MovementTimeRepository movementTimeRepository;
    //Tam gün boyunca geçerli
    private Map<LocalTime, List<MovementTime>> timesByMinute;
    private final Set<Long> triggeredIds = ConcurrentHashMap.newKeySet();

    @Autowired
    public MovementTimeCache(MovementTimeRepository movementTimeRepository) {
        this.movementTimeRepository = movementTimeRepository;
        loadAll();
    }

    //Uygulama başlar başlamaz yükle
    private void loadAll() {
        timesByMinute = movementTimeRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        mt -> mt.getTime().truncatedTo(ChronoUnit.MINUTES)
                ));
    }

    //İstersen her gece yeniden yükle
    @Scheduled(cron = "0 0 0 * * *")
    private void reload() {
        triggeredIds.clear();
        loadAll();
    }

    public List<MovementTime> findByTime(LocalTime time) {
        return timesByMinute.getOrDefault(time.truncatedTo(ChronoUnit.MINUTES), Collections.emptyList());
    }
}
