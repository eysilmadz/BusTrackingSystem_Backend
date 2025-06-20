package com.RotaDurak.RotaDurak.planner;

//Mesafeyi farklı CostType'lara çevirecek yardımcı sınıf
public class CostWeights {
    //varsayılan ortalama hızlar
    public static final double BUS_SPEED_KM_PER_HOUR = 30.0;
    public static final double WALK_SPEED_KM_PER_HOUR = 5.0;

    /**
     * @param distanceKm: iki nokta arasındaki direkt (haversine) mesafe kilometre cinsinden
     * @param type: hangi maliyet tipine çevireceğiz
     * @return ağırlık(weight) değeri
     */

    public static double computeWeight(double distanceKm, CostType type) {
        return switch(type) {
            case DISTANCE -> distanceKm;
            case TIME -> distanceKm / BUS_SPEED_KM_PER_HOUR; //saat cinsinden yolculuk süresi
            case WALK -> distanceKm / WALK_SPEED_KM_PER_HOUR; //saat cinsinden yürüme süresi
        };
    }
}
