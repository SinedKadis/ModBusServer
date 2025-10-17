package com.voltageg;

import com.digitalpetri.modbus.exceptions.UnknownUnitIdException;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.pdu.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.pdu.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.server.ReadWriteModbusServices;


public class Register {
    static ReadWriteModbusServices modbusServices = Server.modbusServices;

    public static void writeRegistries(int unitID,int quantity,int[] data) {
        byte[] toWrite = new byte[quantity*2];
        for (int i = 0,i1 = 0; i < quantity*2; i=i+2,i1++) {
            int b = data[i1];
            int v = b/256;
            int v1 = b%256;
            toWrite[i] = (byte) (v-256);
            toWrite[i+1] = (byte) (v1-256);
        }
        try {
            modbusServices.writeMultipleRegisters(null, unitID,
                    new WriteMultipleRegistersRequest(0, quantity,toWrite));
        } catch (UnknownUnitIdException e) {
            throw new RuntimeException(e);
        }
    }

    public static int[] readRegisters(int unitID,int quantity) {
        ReadHoldingRegistersResponse response;
        try {
            response = modbusServices.readHoldingRegisters(null, unitID,
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
