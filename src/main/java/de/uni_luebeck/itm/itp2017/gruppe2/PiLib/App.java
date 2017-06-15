package de.uni_luebeck.itm.itp2017.gruppe2.PiLib;

import java.io.File;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.util.Scanner;

import org.apache.log4j.lf5.util.StreamUtils;

import com.google.common.base.Objects;
import com.google.common.io.Files;

import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks.Task1_3;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks.Task2;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks.Task3;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Throwable {
		// new Task1_3().run(args);
		// new Task2().run(args);
		if (args.length < 3) {
			System.out.println("Run args: [face|sound] [myIP] \"how to exec the code\"");
		}
		if ("face".equals(args[0])) {
			new Task3(args[1]).run(args);
		} else {
			// TODO start sound server
		}



	}

	static void read() {
		new Thread(() -> {
			try {
				Process process = Runtime.getRuntime().exec("java -jar target/PiLib-0.0.1-SNAPSHOT.jar 2");

				Scanner scanner = new Scanner(process.getInputStream());
				while (true) {

					InputStreamReader r = new InputStreamReader(System.in);
					if (scanner.hasNext()) {
						String face = Objects.firstNonNull(scanner.nextLine(), "nicht bekannt!");
						System.out.println("has scanner new line? " + face);
					}

				}
			} catch (Exception e) {
				System.out.println("dead");
				System.out.println(e.getMessage());

			}
		}).start();
	}

	static void write() {
		new Thread(() -> {
			while (true) {
				long l = 0;
				System.out.println("Writing: Hello " + l++);
			}
		}).start();
	}
}
