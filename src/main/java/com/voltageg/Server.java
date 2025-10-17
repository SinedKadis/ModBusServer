package com.voltageg;

import com.digitalpetri.modbus.serial.server.SerialPortServerTransport;
import com.digitalpetri.modbus.server.*;
import com.digitalpetri.modbus.tcp.server.NettyRtuServerTransport;
import com.digitalpetri.modbus.tcp.server.NettyTcpServerTransport;
import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Server {
    private static final String bindAddress = "127.0.0.1";
    private static final List<ProcessImage> processImages = new ArrayList<>();

    public static final ReadWriteModbusServices modbusServices = new ReadWriteModbusServices() {
        @Override
        public Optional<ProcessImage> getProcessImage(int unitId) {
            while (processImages.size()-1 < unitId) {
                processImages.add(new ProcessImage());
            }
            return Optional.ofNullable(processImages.get(unitId));
        }
    };

    public static ModbusServer startRTUServer() {
        var transport = SerialPortServerTransport.create(cfg -> {
            cfg.setSerialPort("/dev/ttyUSB0");
            cfg.setBaudRate(115200);
            cfg.setDataBits(8);
            cfg.setParity(SerialPort.NO_PARITY);
            cfg.setStopBits(SerialPort.TWO_STOP_BITS);
        });
        var server = ModbusRtuServer.create(transport,modbusServices);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return server;
    }

    public static ModbusServer startTCPServer() {
        var transport = NettyTcpServerTransport.create(cfg -> {
            cfg.setBindAddress(bindAddress);
            cfg.setPort(502);
        });
        var server = ModbusTcpServer.create(transport, modbusServices);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return server;
    }

    public static ModbusServer startRTUOverTCPSlave() {
        var transport = NettyRtuServerTransport.create(cfg -> {
            cfg.setBindAddress(bindAddress);
            cfg.setPort(502);
        });

        var server = ModbusRtuServer.create(transport,modbusServices);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return server;
    }
}
