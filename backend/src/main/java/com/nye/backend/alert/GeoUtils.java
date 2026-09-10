package com.nye.backend.alert;

public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
    }

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Floute une coordonnée à ~1 km près (cahier des charges §12.1), pour les alertes
     * pas encore validées par un administrateur. Déterministe : une même alerte renvoie
     * toujours le même point flouté.
     */
    static double blur(double coordinate) {
        return Math.round(coordinate * 100.0) / 100.0;
    }
}
