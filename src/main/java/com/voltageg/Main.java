package com.voltageg;


import com.digitalpetri.modbus.client.ModbusClient;
import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.exceptions.ModbusExecutionException;
import com.digitalpetri.modbus.exceptions.ModbusResponseException;
import com.digitalpetri.modbus.exceptions.ModbusTimeoutException;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.pdu.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class Main {
    static boolean stop = false;
    private static ModbusClient client;
    static final Supplier<Long> tick = System::currentTimeMillis;
    static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final int tickRate = 500;

    public static void main(String[] args) {
        init();
        while (!stop) {
            if (tick.get() % tickRate == 0 )
                mainLoop();
        }

    }



    private static void init() {
        var transport = NettyTcpClientTransport.create(cfg -> {
            cfg.setHostname("DESKTOP-UU22OM8");
            cfg.setPort(502);
        });

        client = ModbusTcpClient.create(transport);
        try {
            client.connect();
        } catch (ModbusExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mainLoop() {
        ReadHoldingRegistersResponse response;
        try {
            response = client.readHoldingRegisters(
                    1,
                    new ReadHoldingRegistersRequest(0, 5)
            );
        } catch (ModbusExecutionException | ModbusResponseException | ModbusTimeoutException e) {
            throw new RuntimeException(e);
        }
        byte[] registers = response.registers();
        StringBuilder string = new StringBuilder("Bytes: ");
        for (int i = 0; i < registers.length; i=i+2) {
            int b = registers[i];
            int b1 = registers[i+1];
            if (b<0) b = 256+b;
            if (b1<0) b1 = 256+b1;
            string.append((b<<8)+b1).append(", ");

//            if (b<9) string.append(0);
//            if (b<99) string.append(0);
//            string.append(b).append(":");
//            if (b1<9) string.append(0);
//            if (b1<99) string.append(0);
//            string.append(b1).append(", ");
        }
        LOGGER.info(string.toString());
        try {
            client.writeMultipleRegisters(
                    2,
                    new WriteMultipleRegistersRequest(2, 5, new byte[]{1, 2, 22, -123, 23, 0, 0, 0, 0, 0}));
        } catch (ModbusExecutionException | ModbusResponseException | ModbusTimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}