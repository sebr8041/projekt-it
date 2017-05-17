package de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.pojo.InnerResults;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.pojo.SSPRestResult;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.util.RestClient;
import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.endpoint.CoapEndpoint;
import de.uzl.itm.ncoap.message.CoapRequest;
import de.uzl.itm.ncoap.message.CoapResponse;
import de.uzl.itm.ncoap.message.MessageCode;
import de.uzl.itm.ncoap.message.MessageType;

public class Task2_1 implements ITask {

	@Option(name = "--host", usage = "Host of the SSP (ip or domain)")
	private String SSP_HOST = "141.83.151.196";

	@Option(name = "--port", usage = "Port of the SSP")
	private int SSP_PORT = 5683;

	@Override
	public void run(String[] args) throws Throwable {
		// The args4j command line parser
		CmdLineParser parser = new CmdLineParser(Task2_1.class);
		parser.setUsageWidth(80);

		// Parse the arguments
		try {
			parser.parseArgument(args);
			Task1_3 task = new Task1_3();
			task.run(new String[0]);
			// create the coap server
			new Server(task);
			// calculate average
			calculateAvg(task);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts a thread that periodically asks the SSP for ldr average
	 * 
	 * @param task
	 */
	protected void calculateAvg(Task1_3 task) {
		new Thread(() -> {
			ObjectMapper objectMapper = new ObjectMapper();
			String sparql = "PREFIX itm: <https://pit.itm.uni-luebeck.de/>\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
					+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +

					"SELECT  ( AVG(xsd:float(?value)) AS ?v ) WHERE {" + "?comp rdf:type itm:Component."
					+ "?comp itm:isType \"LDR\"^^xsd:string." + "?comp itm:hasStatus ?status."
					+ "?status itm:hasValue ?value." + "}";
			String port = "8080";

			RestClient rc = new RestClient(SSP_HOST, port, sparql, "application/sparql-results+json");

			while (true) {
				try {
					LinkedList<String> ll;
					ll = rc.getResult();

					for (String s : ll) {

						SSPRestResult r = objectMapper.readValue(s, SSPRestResult.class);
						InnerResults inner = objectMapper.readValue(r.getResults(), InnerResults.class);
//						System.out.println("++++++++++++++++++++" + r.getResults());
						String value = inner.getResults().getBindings().get(0).get("v").getValue();
						System.out.println("AVG: " + value);
						task.setAvgValue(Float.parseFloat(value));

					}
					// sleep a second
					Thread.sleep(1000);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

	}

	/**
	 * COAP-Endpoint
	 * 
	 * @author drickert
	 *
	 */
	class Server extends CoapEndpoint {

		/**
		 * Constructor
		 * 
		 * @param task
		 *            The task to get the current ldr-value from to access the
		 *            led
		 * @throws IllegalArgumentException
		 * @throws NoSuchFieldException
		 * @throws SecurityException
		 * @throws IllegalAccessException
		 * @throws URISyntaxException
		 */
		Server(Task1_3 task) throws IllegalArgumentException, NoSuchFieldException, SecurityException,
				IllegalAccessException, URISyntaxException {
			super();
			// create a resource at /ldr
			registerWebresource(new SimpleObservableLightService("/ldr", 2, this.getExecutor(), task));
			// create a resource at /utc
			registerWebresource(new SimpleObservableTimeService("/utc", 5, this.getExecutor()));
			// register at SSP
			registerAtSSP();

		}

		public void registerAtSSP() throws URISyntaxException {

			URI resourceURI = new URI("coap", null, SSP_HOST, SSP_PORT, "/registry", null, null);
			System.out.println(resourceURI.toString());
			CoapRequest coapRequest = new CoapRequest(MessageType.CON, MessageCode.POST, resourceURI);
			InetSocketAddress remoteSocket = new InetSocketAddress(SSP_HOST, SSP_PORT);

			this.sendCoapRequest(coapRequest, remoteSocket, new ClientCallback() {

				@Override
				public void processCoapResponse(CoapResponse coapResponse) {
					System.out.println("received response" + coapResponse.toString());

				}
			});
		}
	}

}
