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
import net.sf.yad2xx.ft4222.SpiCPhase;
import net.sf.yad2xx.ft4222.SpiCPolarity;
import net.sf.yad2xx.ft4222.SpiClock;
import net.sf.yad2xx.ft4222.SpiMode;

/**
 * @author Stephen Davies
 * @since May 2020
 * @since 2.1
 */
public class SpiMasterSample {

    private SpiMasterSample() {
    }

    public static void main(String[] args) {
        try {
            List<FT4222Device> devices = selectDevices();

            if (!devices.isEmpty()) {
                FT4222Device dev = devices.get(0);

                System.out.println(dev);

                dev.open();
                System.out.println();

                dev.spiMasterInit(SpiMode.SPI_IO_SINGLE,
                        SpiClock.CLK_DIV_512,
                        SpiCPolarity.CLK_IDLE_LOW,
                        SpiCPhase.CLK_LEADING,
                        1);

                dev.spiMasterSetLines(SpiMode.SPI_IO_DUAL);
                dev.spiMasterSetLines(SpiMode.SPI_IO_QUAD);
                dev.spiMasterSetLines(SpiMode.SPI_IO_SINGLE);

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
