package de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

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

			new Server();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class Server extends CoapEndpoint {
		
		Server() throws IllegalArgumentException, NoSuchFieldException, SecurityException, IllegalAccessException, URISyntaxException {
			super();
			registerWebresource(new SimpleObservableLightService("/ldr", 2, this.getExecutor()));
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
					System.out.println("received response"+ coapResponse.toString());
					
				}
			});
		}
	}
	
}
