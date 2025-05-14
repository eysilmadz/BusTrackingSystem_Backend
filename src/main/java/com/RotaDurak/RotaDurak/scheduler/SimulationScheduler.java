package com.RotaDurak.RotaDurak.scheduler;

import com.RotaDurak.RotaDurak.model.Direction;
import com.RotaDurak.RotaDurak.model.MovementTime;
import com.RotaDurak.RotaDurak.repository.MovementTimeRepository;
import com.RotaDurak.RotaDurak.service.BusSimulationService;
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

    @Autowired private MovementTimeRepository movementTimeRepository;
    @Autowired private BusSimulationService busSimulationService;
    // Bu küme, zaten tetiklenmiş MovementTime.id'lerini tutar:
    private final Set<Long> triggeredIds = ConcurrentHashMap.newKeySet();
    /**
     * Her dakikanın 0. saniyesinde tetiklenir.
     * Böylece saat:dakika eşleşmesini tam zamanında yakalarız.
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkAndLaunchSimulations() {
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        int hour = now.getHour();
        int minute = now.getMinute();
        log.info("⏲️ Scheduler tetiklendi → Şu an {}:{}", hour, minute);

        // 2) Tüm rota ve yönler için, tam bu saate denk gelen kayıtları çek
        List<MovementTime> dueList =
                movementTimeRepository.findByHourAndMinute(hour, minute);

        if (dueList.isEmpty()) {
            log.info("   Hiç kalkış kaydı yok, sonraki dakikayı bekliyoruz...");
            return;
        }

        // 3) Aynı rota+yön için birden fazla kayıt varsa yalnızca birini al:
        Map<Pair<Long,Direction>, MovementTime> unique =
                dueList.stream().collect(Collectors.toMap(
                        mt -> Pair.of(mt.getRoute().getId(), mt.getDirection()),
                        mt -> mt,
                        (first, second) -> first    // çakışma olursa ilki kalsın
                ));

        // 4) Her benzersiz kaydı tetikle, ama sadece daha önce tetiklenmemişse
        unique.values().forEach(mt -> {
            if (triggeredIds.add(mt.getId())) {
                Long routeId  = mt.getRoute().getId();
                Direction dir = mt.getDirection();
                log.info("✅ Simülasyon başlatılıyor → routeId={}, direction={}, time={}",
                        routeId, dir, mt.getTime());
                busSimulationService.simulateRoute(
                        routeId,
                        dir,
                        DEFAULT_DWELL_SECONDS,
                        DEFAULT_AVG_SPEED_KMH
                );
            } else {
                log.info("ℹ️ MT#{} zaten tetiklenmiş, atlanıyor.", mt.getId());
            }
        });
    }


}
