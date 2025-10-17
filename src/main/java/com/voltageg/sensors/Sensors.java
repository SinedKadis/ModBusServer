package com.voltageg.sensors;

import com.voltageg.Register;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Sensors {
    TEMPERATURE_HUMIDITY(1,new SensorData(
            Pair.of(-40,85),
            Pair.of(0,85)));

    private final int unitID;
    private final SensorData sensorData;

    Sensors(int unitID, SensorData sensorData) {
        this.unitID = unitID;
        this.sensorData = sensorData;
    }

    public void writeData(int... data) {
        if (this.sensorData.measurements().size() < data.length){
            throw new RuntimeException("To many data:"+ Arrays.toString(data));
        }
        int[] toWrite = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            Pair pair = this.sensorData.measurements().get(i);
            int d = Math.clamp(data[i],pair.getFirst(),pair.getSecond());
            d += Math.abs(pair.getFirst());
            toWrite[i] = d;
        }
        Register.writeRegistries(this.unitID,data.length,toWrite);
    }
    public int[] readData() {
        int[] data = Register.readRegisters(this.unitID,this.sensorData.measurements().size());
        int[] toReturn = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            Pair pair = this.sensorData.measurements().get(i);
            int d = data[i] - Math.abs(pair.getFirst());
            toReturn[i] = d;
        }
        return toReturn;

    }


}
