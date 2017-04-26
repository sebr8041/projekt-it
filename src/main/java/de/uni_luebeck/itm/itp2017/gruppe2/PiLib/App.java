package de.uni_luebeck.itm.itp2017.gruppe2.PiLib;

import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks.ITask;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks.Task1_2;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks.Task1_3;

/**
 * Hello world!
 *
 */
public class App {
	private static final ITask[] tasks = new ITask[]{new Task1_2(), new Task1_3()};
	public static void main(String[] args) throws Throwable {
		if(args.length > 0) {
			tasks[Integer.parseInt(args[0])].run(args);
		}
	}

}
