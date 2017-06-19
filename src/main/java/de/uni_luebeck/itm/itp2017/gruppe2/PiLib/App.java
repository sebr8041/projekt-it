package de.uni_luebeck.itm.itp2017.gruppe2.PiLib;

import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks.FaceTask;

/**
 * Start-Point
 */
public class App {
	public static void main(String[] args) throws Throwable {
		if (args.length < 1) {
			System.out.println("Run args: [face|sound]");
		}
		if ("face".equals(args[0])) {
			new FaceTask().run(args);
		} else {
			// TODO start sound server
		}
	}
}
