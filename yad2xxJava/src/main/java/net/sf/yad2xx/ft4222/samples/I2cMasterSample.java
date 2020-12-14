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

                byte[] array = {0x06, 0x01, 0x01};
                byte[] array1 = {0x06, 0x01, 0x02};
                byte[] array2 = {0x10, 0x02, 0x7D, 0x01};

        int length = 5;
        byte[] buffer = new byte[length];
        int length1 = 3;
        byte[] buffer1 = new byte[length1];

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
                dev1.gpioInit();                    //Init device B as GPIO

                System.out.println("Enable Interrupt, GPIO3 acts as an input pin");
                dev1.setWakeUpInterrupt(true);
                dev1.setInterruptTrigger(GpioTrigger.GPIO_TRIGGER_LEVEL_HIGH);

                dev.i2cMasterReadAndPrint(buffer, length); // Read out 5 bytes after boot

            } else {
                System.out.println("No FT4222 device is found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<FT4222Device> devices = selectDevices();
        FT4222Device dev = (FT4222Device) devices.get(0);
        FT4222Device dev1 = (FT4222Device) devices.get(1);


        dev.i2cMasterWriteAndPrint(array);
        TimeUnit.MILLISECONDS.sleep(1000);//Time beetween ON/OFF

        int i = 0;
        while(true)
    {
        if (dev1.gpioGetTriggerStatus() != 0)
        {
            dev.i2cMasterReadAndPrint(buffer1, length1);   // read response,
            i = 1;
        }
        else if(i == 1)
        {
            System.out.println("Unitialize FT4222");
            System.out.println("UnInitialize FT4222");
            dev.unInitialize();
            dev1.unInitialize();
            System.out.println("Close FT device");
            dev.close();
            dev1.close();
        }
    }
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
}



