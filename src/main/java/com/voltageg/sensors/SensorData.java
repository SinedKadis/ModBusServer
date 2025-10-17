package com.voltageg.sensors;

import java.util.Arrays;
import java.util.List;

public record SensorData(List<Pair> measurements) {
    public SensorData(Pair... measurements) {
        this(Arrays.asList(measurements));
    }
}
