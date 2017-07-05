package de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks;

import java.io.FileInputStream;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.pojo.InnerResults;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.pojo.SSPRestResult;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.util.Configuration;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.util.RestClient;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class SoundTask implements ITask {

	private String SSP_HOST = "141.83.151.196";
	private Thread soundThread;
	private static final double BORDER_TEMPERATURE = 10.0;
	private String lastUser = "";
	private Temperature currentTemperature = Temperature.Cold;

	@Override
	public void run(Configuration config) throws Throwable {
		SSP_HOST = config.getSSP_HOST();

		findFaces();
		findTemprature();
	}

	private enum Temperature {
		Warm("warm"), Cold("cold");
		private String tempName;

		Temperature(String tempName) {
			this.tempName = tempName;
		}

		String getTempName() {
			return tempName;
		}
	};

	private void findTemprature() {
		// object mapper to map json to objects
		ObjectMapper objectMapper = new ObjectMapper();

		String sparql = "PREFIX gruppe2: <http://gruppe02.pit.itm.uni-luebeck.de/>"
				+ "PREFIX itm: <https://pit.itm.uni-luebeck.de/>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX gruppe1: <http://gruppe01.pit.itm.uni-luebeck.de/>" + "SELECT ?temp WHERE {"
				+ "gruppe1:tempSensor itm:hasStatus ?status." + "?status itm:hasValue ?temp."
				+ "?status itm:hasScaleUnit \"Celcius\"^^xsd:string." + "}";

		String port = "8080";

		RestClient rc = new RestClient(SSP_HOST, port, sparql, "application/sparql-results+json");

		new Thread(() -> {
			while (true) {
				try {
					LinkedList<String> ll;
					// get result lines from query
					ll = rc.getResult();
					// for each result
					for (String s : ll) {
						// map resulting json object to java object using
						// objectMapper
						SSPRestResult r = objectMapper.readValue(s, SSPRestResult.class);
						// map the result inside the SSPRestResult to
						// Java-Object
						InnerResults inner = objectMapper.readValue(r.getResults(), InnerResults.class);
						// get value of variable v from the result
						if (inner.getResults().getBindings().isEmpty()) {
							System.out.println("No results found for temperature!");
							continue;
						}
						String value = inner.getResults().getBindings().get(0).get("temp").getValue();
						if (Double.parseDouble(value) < BORDER_TEMPERATURE) {
							currentTemperature = Temperature.Cold;
						} else {
							currentTemperature = Temperature.Warm;
						}
					}
					// sleep a second
					Thread.sleep(10000);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public synchronized void playMusicFor(String name) {

		String mp3 = "/opt/projekt-it/data/" + name + "/" + currentTemperature.getTempName();
		if (soundThread != null) {
			soundThread.stop();
		}
		soundThread = new Thread(new Mp3Player(mp3));
		soundThread.start();

	}

	class Mp3Player implements Runnable {

		private String filename;
		private AdvancedPlayer player;

		Mp3Player(String filename) {
			this.filename = filename;
		}

		public void play() {
			try {
				System.out.println("before buffer");
				FileInputStream buffer = new FileInputStream(filename);
				System.out.println("new advanced player");
				player = new AdvancedPlayer(buffer);
				System.out.println("play");
				player.play();
				System.out.println("after play");

			} catch (Exception e) {

				System.out.println(e);
			}

		}

		@Override
		public void run() {
			while (true) {
				play();
			}
		}

	}

	protected void findFaces() {
		new Thread(() -> {
			// object mapper to map json to objects
			ObjectMapper objectMapper = new ObjectMapper();

			String sparql = "" + "PREFIX gruppe2: <http://gruppe02.pit.itm.uni-luebeck.de/>"
					+ "PREFIX itm: <https://pit.itm.uni-luebeck.de/>"
					+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +

					"SELECT ?name WHERE {" + "?cam itm:isType \"Camera\"^^xsd:string." + "?cam itm:hasStatus ?status."
					+ "?status itm:hasValue ?name." + "?status itm:hasScaleUnit \"UID\"^^xsd:string." + "}";

			String port = "8080";

			RestClient rc = new RestClient(SSP_HOST, port, sparql, "application/sparql-results+json");

			while (true) {
				System.out.println("new iteration+");
				try {
					LinkedList<String> ll;
					// get result lines from query
					ll = rc.getResult();
					// for each result
					for (String s : ll) {
						// map resulting json object to java object using
						// objectMapper
						SSPRestResult r = objectMapper.readValue(s, SSPRestResult.class);
						// map the result inside the SSPRestResult to
						// Java-Object
						InnerResults inner = objectMapper.readValue(r.getResults(), InnerResults.class);
						// System.out.println("++++++++++++++++++++" +
						// r.getResults());
						// get value of variable v from the result
						if (inner.getResults().getBindings().isEmpty()) {
							System.out.println("No results found!");
							continue;
						}
						String value = inner.getResults().getBindings().get(0).get("name").getValue();
						if (!FaceTask.UNKNOWN.equals(value)) {
							System.out.println("Playing music for: " + value + " ("+currentTemperature.getTempName()+")");
							if (!lastUser.equals(value)) {
								playMusicFor(value);
								lastUser = value;
							}
						} else {
							System.out.println("Dont play music: unknown");
						}
					}
					// sleep a second
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

	}

}
