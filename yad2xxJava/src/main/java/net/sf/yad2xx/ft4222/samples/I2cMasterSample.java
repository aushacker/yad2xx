/*
 * Copyright 2020 Stephen Davies
 *
 * This file is part of yad2xx.
 *
 * yad2xx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * yad2xx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with yad2xx. If not, see <https://www.gnu.org/licenses/>.
 */

package net.sf.yad2xx.ft4222.samples;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FT4222Device;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;
import net.sf.yad2xx.ft4222.GpioTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
/**
 * Functionally equivalent to the FTDI Chip i2c_master.cpp example.
 * <p>
 * Tweaked a little bit to work with a TI PCF8575 I2C IO device
 *
 * @author Stephen Davies
 * @since May 2020
 * @since 2.1
 */
public final class I2cMasterSample {

    private I2cMasterSample() {
    }

    /**
     * I2C bus date rate.
     */
    private static final int BIT_RATE_400K = 400;


    public static void main(String[] args) throws FTDIException, InterruptedException {
                byte[] NORMAL_RESET = {0x00, 0x01, 0x00};

        byte[] RF_ON  = {0x06, 0x01, 0x01};
                byte[] array1 = {0x06, 0x01, 0x02};
                byte[] array2 = {0x10, 0x02, 0x7D, 0x01};

        int length = 5;
        byte[] buffer = new byte[length];

        int InstructionAndPayLoadLength = 2;
        byte[] InstructionAndPayLoadBuffer = new byte[InstructionAndPayLoadLength];
        int payload = InstructionAndPayLoadBuffer[1];
        byte[] payloadBuffer = new byte[payload];


        int status = 0;
        String hex;

        try
        {
            List<FT4222Device> devices = selectDevices();

            if (!devices.isEmpty()) {

                FT4222Device dev = devices.get(0);  //Get device A description
                FT4222Device dev1 = devices.get(1); //Get device B description

                System.out.println(dev);
                System.out.println(dev1);

                dev.open();                         //Open device A
                dev1.open();                        //Open device B

                System.out.println("Init FT4222A as I2C master");
                dev.i2cMasterInit(BIT_RATE_400K);   //Init device A as I2cMaster


                System.out.println("Init FT4222B as GPIO");
                dev1.gpioInit();   //Init device B as GPIO

                System.out.println("Enable Interrupt, GPIO3 acts as an input pin");
                dev1.setWakeUpInterrupt(true);
                dev1.setInterruptTrigger(GpioTrigger.GPIO_TRIGGER_LEVEL_HIGH);

               // dev.i2cMasterReadAndPrint(buffer, length); // Read out 5 bytes after boot

            } else {
                System.out.println("No FT4222 device is found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<FT4222Device> devices = selectDevices();
        FT4222Device dev = (FT4222Device) devices.get(0);
        FT4222Device dev1 = (FT4222Device) devices.get(1);

        boolean flag = false;
       //  dev.i2cMasterReadAndPrint(buffer, length); // Read out 5 bytes after boot

       // dev.i2cMasterWriteAndPrint(NORMAL_RESET);

        for(int i = 0; i < 100; i++){
            TimeUnit.MILLISECONDS.sleep(1);

            if(dev1.gpioGetTriggerStatus() != 0){
                dev.i2cMasterReadAndPrint(InstructionAndPayLoadBuffer, InstructionAndPayLoadLength);   // read response,
                status = dev.i2cMasterGetStatus();
                hex = Integer.toHexString(status);
                System.out.println(ANSI_PURPLE + "I2cMasterStatus: " + hex + " " + i + "ms" + " flag: " + flag + ANSI_RESET);;

                if(InstructionAndPayLoadBuffer[0] == 0x40) // response
              {
                  payload = InstructionAndPayLoadBuffer[1];
                  payloadBuffer = new byte[payload];
                  dev.i2cMasterReadAndPrint(payloadBuffer, payload);   // read response,
                  status = 0;
                  hex = "";

                  status = dev.i2cMasterGetStatus();
                  hex = Integer.toHexString(status);
                  System.out.println(ANSI_PURPLE + "I2cMasterStatus: " + hex + " " + i + "ms" + " flag: " + flag + ANSI_RESET);;

                  System.out.println(ANSI_RED +"Response: " + bytesToHex(InstructionAndPayLoadBuffer) + bytesToHex(payloadBuffer, false) + " flag: " + flag + " " + i + "ms" + ANSI_RESET);
              }
              else if(InstructionAndPayLoadBuffer[0] == -128) // event
              {
                  payload = InstructionAndPayLoadBuffer[1];
                  payloadBuffer = new byte[payload];
                  dev.i2cMasterReadAndPrint(payloadBuffer, payload);   // read response,
                  status = 0;
                  hex = "";

                  status = dev.i2cMasterGetStatus();
                  hex = Integer.toHexString(status);
                  System.out.println(ANSI_PURPLE + "I2cMasterStatus: " + hex + " " + i + "ms" + " flag: " + flag + ANSI_RESET);;

                  flag = true;
                  System.out.println(ANSI_BLUE + "Event: " + bytesToHex(InstructionAndPayLoadBuffer) + bytesToHex(payloadBuffer, false) + " flag: " + flag + " " + i + "ms" + ANSI_RESET);
              }

            }
            if(payloadBuffer[0] != 0x00 && flag){
                dev.i2cMasterWriteAndPrint(RF_ON);
                status = 0;
                hex = "";

                status = dev.i2cMasterGetStatus();
                hex = Integer.toHexString(status);
                System.out.println(ANSI_PURPLE + "I2cMasterStatus: " + hex + " " + i + "ms" + " flag: " + flag + ANSI_RESET);;
                System.out.println(ANSI_GREEN + "Command: " + bytesToHex(NORMAL_RESET) + " " + i + " ms" + " EventFlag: " + flag  + ANSI_RESET);

                flag = false;
            }
            else if(payloadBuffer[0] == 0x00 && flag){
                i = 1001;
            }
        }
        /*
        while(dev1.gpioGetTriggerStatus() != 0)
        status = dev.i2cMasterGetStatus();
        hex = Integer.toHexString(status);
        System.out.println("Status after first i2cWrite executed" + hex);
        status = 0;
        hex = "";
        TimeUnit.MILLISECONDS.sleep(1000);//Time beetween ON/OFF

        int i = 0;
        while(true)
    {
        if (dev1.gpioGetTriggerStatus() != 0)
        {
            dev.i2cMasterReadAndPrint(buffer1, length1);   // read response,
            status = dev.i2cMasterGetStatus();
            hex = Integer.toHexString(status);
            System.out.println("Status" + hex);
            status = 0;
            hex = "";

            i = 1;
        }

         */
       // else if(i == 1)
            System.out.println("Unitialize FT4222");
            System.out.println("UnInitialize FT4222");
            dev.unInitialize();
            dev1.unInitialize();
            System.out.println("Close FT device");
            dev.close();
            dev1.close();

}

    /**
     * @return list of attached FT4222 devices.
     */
    private static List<FT4222Device> selectDevices() throws FTDIException {
        List<FT4222Device> devices = new ArrayList<>();

        for (Device dev : FTDIInterface.getDevices()) {
            if (dev instanceof FT4222Device) {
                devices.add((FT4222Device) dev);
            }
        }

        return devices;
    }
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, true);
    }

    public static String bytesToHex(byte[] bytes, boolean include0x) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return (include0x ? "0x" : "") + new String(hexChars);
    }



    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
}



