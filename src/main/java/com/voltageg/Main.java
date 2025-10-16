package com.voltageg;


import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.exceptions.ModbusExecutionException;
import com.digitalpetri.modbus.exceptions.ModbusResponseException;
import com.digitalpetri.modbus.exceptions.ModbusTimeoutException;
import com.digitalpetri.modbus.exceptions.UnknownUnitIdException;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.pdu.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.pdu.WriteMultipleRegistersResponse;
import com.digitalpetri.modbus.server.*;
import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
import com.digitalpetri.modbus.tcp.server.NettyTcpServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class Main {
    static boolean stop = false;
    private static ModbusServer server;
    static final Supplier<Long> tick = System::currentTimeMillis;
    static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    static ProcessImage processImage;
    static ReadWriteModbusServices modbusServices;
    private static final int tickRate = 500;

    public static void main(String[] args) {
        init();
        while (!stop) {
            if (tick.get() % tickRate == 0 )
                mainLoop();
        }

    }



    private static void init() {
        var transport = NettyTcpServerTransport.create(cfg -> {
            cfg.setBindAddress("DESKTOP-UU22OM8");
            cfg.setPort(502);
        });
        processImage = new ProcessImage();
        modbusServices = new ReadWriteModbusServices() {
            @Override
            protected Optional<ProcessImage> getProcessImage(int unitId) {
                return Optional.of(processImage);
            }
        };
        server = ModbusTcpServer.create(transport, modbusServices);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        writeRegistries(5,new int[]{1,2,3,4,5});
    }

    private static void mainLoop() {
        int[] registers = readRegisters(5);
        for (int j = 0; j < registers.length; j++) {
            int i = registers[j];
            i = i + 1;
            if (i > 65535) i = 0;
            registers[j] = i;
        }
        writeRegistries(5,registers);

    }

    private static void writeRegistries(int quantity,int[] data) {
        byte[] toWrite = new byte[quantity*2];
        for (int i = 0,i1 = 0; i < quantity*2; i=i+2,i1++) {
            int b = data[i1];
            int v = b/256;
            int v1 = b%256;
            toWrite[i] = (byte) (v-256);
            toWrite[i+1] = (byte) (v1-256);
        }
        try {
            modbusServices.writeMultipleRegisters(null, 1,
                            new WriteMultipleRegistersRequest(0, quantity,toWrite));
        } catch (UnknownUnitIdException e) {
            throw new RuntimeException(e);
        }
    }

    private static int[] readRegisters(int quantity) {
        ReadHoldingRegistersResponse response;
        try {
            response = modbusServices.readHoldingRegisters(null, 1,
                    new ReadHoldingRegistersRequest(0, quantity));
        } catch (UnknownUnitIdException e) {
            throw new RuntimeException(e);
        }
        byte[] registers = response.registers();
        int[] toReturn = new int[quantity];
        for (int i = 0,i1 = 0; i < registers.length; i=i+2,i1++) {
            int b = registers[i];
            int b1 = registers[i+1];
            if (b<0) b = 256+b;
            if (b1<0) b1 = 256+b1;
            toReturn[i1] = (b<<8)+b1;
        }
        return toReturn;
    }
}