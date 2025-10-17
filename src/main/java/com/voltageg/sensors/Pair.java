package com.voltageg.sensors;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Pair {
    private int first;
    private int second;
    private Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public static Pair of(int first, int second) {
        return new Pair(first,second);
    }


}
