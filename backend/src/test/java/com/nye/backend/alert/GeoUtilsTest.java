package com.nye.backend.alert;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoUtilsTest {

    @Test
    void distanceKm_isZero_forIdenticalPoints() {
        double distance = GeoUtils.distanceKm(12.6392, -8.0029, 12.6392, -8.0029);
        assertThat(distance).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    void distanceKm_matchesKnownDistance_betweenTwoBamakoPoints() {
        // ~1.2 km apart in reality; verify the haversine result lands in a sane range.
        double distance = GeoUtils.distanceKm(12.6392, -8.0029, 12.6500, -8.0000);
        assertThat(distance).isBetween(1.0, 1.5);
    }

    @Test
    void distanceKm_growsWithSeparation() {
        double near = GeoUtils.distanceKm(12.6392, -8.0029, 12.6400, -8.0000);
        double far = GeoUtils.distanceKm(12.6392, -8.0029, 13.9000, -8.5000);
        assertThat(far).isGreaterThan(near);
    }

    @Test
    void blur_roundsToTwoDecimalPlaces() {
        assertThat(GeoUtils.blur(12.639217)).isEqualTo(12.64);
        assertThat(GeoUtils.blur(-8.002913)).isEqualTo(-8.0);
    }

    @Test
    void blur_isDeterministic_forTheSameInput() {
        assertThat(GeoUtils.blur(12.6455)).isEqualTo(GeoUtils.blur(12.6455));
    }
}
