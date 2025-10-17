package com.voltageg;


import com.digitalpetri.modbus.server.*;
import com.voltageg.sensors.Sensors;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;


public class Main {
    @Setter
    public static boolean stop = false;
    public static final Supplier<Long> tick = System::currentTimeMillis;
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final int tickRate = 50;


    static boolean ticked = false;
    public static void main(String[] args) {
        init();
        while (!stop) {
            if (tick.get() % tickRate == 0) {
                if (!ticked) {
                    tickLoop();
                    ticked = true;
                }
            } else ticked = false;
            mainLoop();
        }

    }

    private static void mainLoop() {



    }

    private static void init() {

        //Server.startTCPServer();
        //Server.startRTUServer();
        Server.startRTUOverTCPSlave();

        Sensors.TEMPERATURE_HUMIDITY.writeData(20,40);

    }



    private static void tickLoop() {
        int temperature = (int) (Math.sin(tick.get())*10+10);
        int humidity = (int) (Math.cos(tick.get())*10+10);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= 19; i++) {
            if (i == temperature) builder.append("T");
            else builder.append(".");
        }
        LOGGER.info(builder.toString());
        builder = new StringBuilder();
        for (int i = 0; i <= 19; i++) {
            if (i == humidity) builder.append("H");
            else builder.append(".");
        }
        LOGGER.info(builder.toString());

        Sensors.TEMPERATURE_HUMIDITY.writeData(temperature,humidity);
    }
}