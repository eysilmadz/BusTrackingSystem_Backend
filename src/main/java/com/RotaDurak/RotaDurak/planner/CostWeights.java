package com.RotaDurak.RotaDurak.planner;

//Mesafeyi farklı CostType'lara çevirecek yardımcı sınıf
public class CostWeights {
    // Varsayılan ortalama hızlar
    public static final double BUS_SPEED_KM_PER_HOUR = 30.0;
    public static final double WALK_SPEED_KM_PER_HOUR = 5.0;

    // Aktarma (hat değişimi) cezaları - saat cinsinden
    public static final double TRANSFER_PENALTY_TIME = 5.0 / 60.0;   // 5 dakika
    public static final double TRANSFER_PENALTY_WALK = 20.0 / 60.0;  // 20 dakika (yürümeyi caydır)
    public static final double TRANSFER_PENALTY_DISTANCE = 0.3;       // 300 metre eşdeğeri

    /**
     * @param distanceKm: iki nokta arasındaki direkt (haversine) mesafe kilometre cinsinden
     * @param type: hangi maliyet tipine çevireceğiz
     * @param isWalking: yürüyüş mu değil mi, yürüyüşse hız yürüyüş hızı olarak hesaplanır
     * @return ağırlık(weight) değeri (saat cinsinden)
     */
    public static double computeWeight(double distanceKm, CostType type, boolean isWalking) {
        return switch (type) {
            case DISTANCE -> distanceKm;
            case TIME -> isWalking
                    ? distanceKm / WALK_SPEED_KM_PER_HOUR
                    : distanceKm / BUS_SPEED_KM_PER_HOUR;
            case WALK -> isWalking
                    ? distanceKm / WALK_SPEED_KM_PER_HOUR
                    : 0.0; // Otobüste yürüme maliyeti yok
        };
    }

    public static double getTransferPenalty(CostType type) {
        return switch (type) {
            case TIME     -> TRANSFER_PENALTY_TIME;
            case WALK     -> TRANSFER_PENALTY_WALK;
            case DISTANCE -> TRANSFER_PENALTY_DISTANCE;
        };
    }
}
