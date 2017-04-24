package de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks;

import java.util.Observable;
import java.util.Observer;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import de.dennis_boldt.RXTX;

public class Task1_3 implements Observer {

	@Option(name = "--ports", usage = "Set USB ports")
	public String ports = null;

	@Option(name = "--rxtxlib", usage = "Set RXTX lib")
	public String rxtxlib = "/usr/lib/jni";

	@Option(name = "--baud", usage = "Set baud rate")
	public int baud = 115200;

	public Task1_3(String[] args)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		RXTX rxtx;
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			rxtx = new RXTX(this.baud);
			rxtx.start(this.ports, this.rxtxlib, this);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
		}
	}

	public void update(Observable o, Object arg) {
		if (arg instanceof byte[]) {
			byte[] bytes = (byte[]) arg;
			System.out.print(new String(bytes));
			float parseFloat = Float.parseFloat(new String(bytes));
			if (parseFloat < 0.01) {
				System.out.println("Gute NaCHT");
			}
		}
	}
}
