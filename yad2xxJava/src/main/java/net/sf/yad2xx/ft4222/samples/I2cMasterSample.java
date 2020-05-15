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

import java.util.ArrayList;
import java.util.List;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FT4222Device;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;

/**
 * Functionally equivalent to the FTDI Chip i2c_master.cpp example.
 *
 * @author Stephen Davies
 * @since 2020
 */
public final class I2cMasterSample {

    private I2cMasterSample() {
    }

    /**
     * I2C bus date rate.
     */
    private static final int BIT_RATE_400K = 400;

    public static void main(String[] args) {
        try {
            List<FT4222Device> devices = selectDevices();

            if (!devices.isEmpty()) {
                FT4222Device dev = devices.get(0);

                System.out.println(dev);

                dev.open();

                System.out.println("Init FT4222 as I2C master");
                dev.i2cMasterInit(BIT_RATE_400K);

                int slaveAddr = 0x22;
                byte[] masterData = { 0x1A, 0x2B, 0x3C, 0x4D };
                int sizeTransferred = 0;

                System.out.printf("I2C master write data to the slave(%#x)... \n", slaveAddr);
                sizeTransferred = dev.i2cMasterWrite(slaveAddr, masterData);
                System.out.printf("bytes written: %d\n", sizeTransferred);

                System.out.printf("I2C master read data from the slave(%#x)... \n", slaveAddr);
                byte[] slaveData = dev.i2cMasterRead(slaveAddr, 4);

                System.out.print("  slave data: ");
                for (int i = 0; i < slaveData.length; ++i) {
                    System.out.printf("%#x, ", slaveData[i]);
                }
                System.out.println();

                System.out.println("UnInitialize FT4222");
                dev.unInitialize();

                System.out.println("Close FT device");
                dev.close();

            } else {
                System.out.println("No FT4222 device is found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
