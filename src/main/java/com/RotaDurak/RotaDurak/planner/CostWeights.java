package com.RotaDurak.RotaDurak.planner;

//Mesafeyi farklı CostType'lara çevirecek yardımcı sınıf
public class CostWeights {
    // Varsayılan ortalama hızlar
    public static final double BUS_SPEED_KM_PER_HOUR = 30.0;
    public static final double WALK_SPEED_KM_PER_HOUR = 5.0;

    /**
     * @param distanceKm: iki nokta arasındaki direkt (haversine) mesafe kilometre cinsinden
     * @param type: hangi maliyet tipine çevireceğiz
     * @param isWalking: yürüyüş mu değil mi, yürüyüşse hız yürüyüş hızı olarak hesaplanır
     * @return ağırlık(weight) değeri (saat cinsinden)
     */
    public static double computeWeight(double distanceKm, CostType type, boolean isWalking) {
        return switch(type) {
            case DISTANCE -> distanceKm;
            case TIME -> {
                if (isWalking) {
                    yield distanceKm / WALK_SPEED_KM_PER_HOUR;
                } else {
                    yield distanceKm / BUS_SPEED_KM_PER_HOUR;
                }
            }
            case WALK -> distanceKm / WALK_SPEED_KM_PER_HOUR;
        };
    }
}
