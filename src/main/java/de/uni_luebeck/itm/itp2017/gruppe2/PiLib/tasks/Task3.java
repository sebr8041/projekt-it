package de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Scanner;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javah.Util.Exit;

import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.pojo.InnerResults;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.pojo.SSPRestResult;
import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.util.RestClient;
import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.endpoint.CoapEndpoint;
import de.uzl.itm.ncoap.message.CoapRequest;
import de.uzl.itm.ncoap.message.CoapResponse;
import de.uzl.itm.ncoap.message.MessageCode;
import de.uzl.itm.ncoap.message.MessageType;

public class Task3 implements ITask {

	@Option(name = "--host", usage = "Host of the SSP (ip or domain)")
	private String SSP_HOST = "141.83.151.196";

	@Option(name = "--port", usage = "Port of the SSP")
	private int SSP_PORT = 5683;

	private String myIP = "";
	private String execCode = "";

	public Task3(String myIP) {
		super();
		this.myIP = myIP;
	}

	@Override
	public void run(String[] args) throws Throwable {
		// The args4j command line parser
		// CmdLineParser parser = new CmdLineParser(Task3.class);
		// parser.setUsageWidth(80);

		// Parse the arguments
		try {
			this.execCode = args[2];
			// parser.parseArgument(args);

			// create the coap server
			new Server(this);
			// calculate average

		} catch (Exception e) {
			e.printStackTrace();
		}
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
		Server(Task3 task) throws IllegalArgumentException, NoSuchFieldException, SecurityException,
				IllegalAccessException, URISyntaxException {
			super();
			ObservableFaceService webresource = new ObservableFaceService("/face", 5, this.getExecutor(), task.myIP);
			getFaces(webresource);
			registerWebresource(webresource);

			// register at SSP
			registerAtSSP();

		}

		void getFaces(ObservableFaceService webresource) {
			// set default face
			webresource.setFace("default");
			new Thread(() -> {
				Process process;
				try {
					process = Runtime.getRuntime().exec(execCode);
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}
				Scanner scanner = new Scanner(process.getInputStream());
				scanner.useDelimiter(">");
				while (true) {
					try {
						System.out.println("new face?");
						if (scanner.hasNext()) {
							String newFace = scanner.next();
							System.out.println("new face is: "+ newFace);
							webresource.setFace(newFace);
						}else{
							System.out.println("no input available!");
						}
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}

				}
			}).start();

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
