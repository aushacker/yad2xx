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

package net.sf.yad2xx.ft4222;

import java.util.ArrayList;
import java.util.List;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.FT4222Device;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;

/**
 * Tests FT4222 generation functions.
 * <p>
 * An FT4222 device needs to be attached so this needs to be run manually.
 *
 * @author Stephen Davies
 * @since May 2019
 * @since 2.1
 */
public class GeneralFunctionTest {

    public static void main(String[] args) {
        try {
            List<FT4222Device> devices = selectDevices();

            if (!devices.isEmpty()) {
                FT4222Device dev = devices.get(0);

                System.out.println("Testing with:");
                System.out.println(dev);

                dev.open();
 
                System.out.println("version: " + dev.getVersion());

                //
                // System clock
                //
                System.out.println("\nSystem clock functions");
                System.out.println("default clock: " + dev.getClock());
                System.out.println("setting 24MHz");
                dev.setClock(ClockRate.SYS_CLK_24);
                System.out.println("clock: " + dev.getClock());
                System.out.println("setting 48MHz");
                dev.setClock(ClockRate.SYS_CLK_48);
                System.out.println("clock: " + dev.getClock());
                System.out.println("setting 80MHz");
                dev.setClock(ClockRate.SYS_CLK_80);
                System.out.println("clock: " + dev.getClock());
                System.out.println("setting 60MHz");
                dev.setClock(ClockRate.SYS_CLK_60);
                System.out.println("clock: " + dev.getClock());

                System.out.println("Toggling suspendOut");
                dev.setSuspendOut(true);
                dev.setSuspendOut(false);

                System.out.println("Toggling wakeUpInterrupt");
                dev.setWakeUpInterrupt(false);
                dev.setWakeUpInterrupt(true);

                // AN_329 states that this is not sufficient to test the API.
                // For now just testing that the native method gets called and
                // returns ok.
                System.out.println("\nInterrupt condition");
                dev.setInterruptTrigger(GpioTrigger.GPIO_TRIGGER_RISING);
                dev.setInterruptTrigger(GpioTrigger.GPIO_TRIGGER_FALLING);
                dev.setInterruptTrigger(GpioTrigger.GPIO_TRIGGER_LEVEL_HIGH);
                dev.setInterruptTrigger(GpioTrigger.GPIO_TRIGGER_LEVEL_LOW);

                dev.i2cMasterInit(400);
                System.out.println("\nMaximum transfer size: " + dev.getMaxTransferSize());

                dev.unInitialize();
                dev.close();
            }
        }
        catch (Exception e) {
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
