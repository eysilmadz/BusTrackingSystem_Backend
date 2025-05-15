package com.RotaDurak.RotaDurak.scheduler;

import com.RotaDurak.RotaDurak.cache.MovementTimeCache;
import com.RotaDurak.RotaDurak.model.Direction;
import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.repository.MovementTimeRepository;
import com.RotaDurak.RotaDurak.service.BusSimulationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SimulationScheduler {
    private static final int DEFAULT_DWELL_SECONDS = 10;
    private static final double DEFAULT_AVG_SPEED_KMH = 40.0;

    @Autowired private BusSimulationService busSimulationService;
    @Autowired private MovementTimeCache movementTimeCache;
    // Bu küme, zaten tetiklenmiş MovementTime.id'lerini tutar:
    private final Set<Long> triggeredIds = ConcurrentHashMap.newKeySet();
    /**
     * Her dakikanın 0. saniyesinde tetiklenir.
     * Böylece saat:dakika eşleşmesini tam zamanında yakalarız.
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkAndLaunchSimulations() {
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        log.info("⏲️ Scheduler tetiklendi → Şu an {}:{}", now.getHour(), now.getMinute());

        List<MovementTime> dueList = movementTimeCache.findByTime(now);
        if (dueList.isEmpty()) {
            log.info("Hiç kalkış kaydı yok, sonraki dakikayı bekliyoruz...");
            return;
        }

        // Aynı rota+yön için birden fazla kayıt varsa yalnızca birini al:
        Map<Pair<Long,Direction>, MovementTime> unique =
                dueList.stream().collect(Collectors.toMap(
                        mt -> Pair.of(mt.getRoute().getId(), mt.getDirection()),
                        mt -> mt,
                        (first, second) -> first    // çakışma olursa ilki kalsın
                ));

        unique.values().forEach(mt -> {
            if(triggeredIds.add(mt.getId())) {
                log.info("✅ Simülasyon → route={}, dir={}, time={}",
                        mt.getRoute().getId(), mt.getDirection(), mt.getTime());
                busSimulationService.simulateRoute(
                        mt.getRoute().getId(),
                        mt.getDirection(),
                        DEFAULT_DWELL_SECONDS,
                        DEFAULT_AVG_SPEED_KMH
                );
            }
        });
    }


}
