/*
 * Copyright 2015-2018 Stephen Davies
 * 
 * This file is part of yad2xx.
 * 
 * yad2xx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * yad2xx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with yad2xx.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.yad2xx.samples;

import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;
import static net.sf.yad2xx.FTDIConstants.FTDI_VID;

import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Common superclass for command line utilities. Makes Apache Commons CLI
 * processing available to subclasses. Provides options for help (usage)
 * and for specifying a custom PID value.
 * <p>
 * Subclasses MAY:
 * <ul>
 * <li>Provide additional Options, use getOptions to modify.</li>
 * <li>Override handleOptions to add processing for their options.</li>
 * </ul>
 * <p>
 * Implements two command lind options, HELP and PID. The help option
 * displays usage information. The pid option allows the user to select
 * an FTDI device with a custom PID setting.
 * 
 * @author		Stephen Davies
 * @since		23 Jun 2015
 * @since		0.3
 */
public abstract class AbstractSample {
	
	private static final String HELP = "h";
	private static final String PID = "p";
	
	private final Options options;
	private CommandLine commandLine;
	
	public AbstractSample() {
		options = new Options();
		options.addOption(Option.builder(PID)
			.longOpt("pid")
			.hasArg(true)
			.required(false)
			.desc("Custom device PID (in hex)")
			.build());
		options.addOption(Option.builder(HELP)
			.longOpt("help")
			.required(false)
			.build());
	}
	
	public CommandLine getCommandLine() {
		return commandLine;
	}
	
	public Options getOptions() {
		return options;
	}

	/**
	 * Delay millis milliseconds.
	 * 
	 * @param	millis			delay time in ms
	 */
	public void delay(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ignored) {}
	}
	
	public void displayUsage(String app) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(app, options);
	}

	/**
	 * Subclasses may decide to override. Overridden version should call this
	 * method.
	 * 
	 * @return					true if ok to proceed
	 */
	protected boolean handleOptions() {
		if (commandLine.hasOption(HELP)) {
			return false;
		}
		
		if (commandLine.hasOption(PID)) {
			String pidHex = commandLine.getOptionValue(PID);
			int pid = Integer.parseInt(pidHex, 16);
			try {
				FTDIInterface.setVidPid(FTDI_VID, pid);
			}
			catch (FTDIException e) {
				e.printStackTrace(System.err);
				return false;
			}
		}
		return true;
	}
	
	public void printProlog(PrintStream out) throws FTDIException {
		out.print("D2XX Library Version: ");
		out.println(FTDIInterface.getLibraryVersion());

		int[] vidPid = FTDIInterface.getVidPid();
		out.print("VID: 0x");
		out.print(Integer.toHexString(vidPid[0]));
		out.print(" PID: 0x");
		out.println(Integer.toHexString(vidPid[1]));
		out.println();
	}
	
	public boolean  processOptions(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		commandLine = parser.parse(getOptions(), args);
		
		return handleOptions();
	}
}
