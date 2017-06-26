package de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.kohsuke.args4j.Option;

import de.uni_luebeck.itm.itp2017.gruppe2.PiLib.util.Configuration;
import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.endpoint.CoapEndpoint;
import de.uzl.itm.ncoap.message.CoapRequest;
import de.uzl.itm.ncoap.message.CoapResponse;
import de.uzl.itm.ncoap.message.MessageCode;
import de.uzl.itm.ncoap.message.MessageType;

/**
 * FaceTask
 * <p>
 * This Task provides the /face enpoint for the SSP and reads the current users
 * name from {@link System#in}
 * 
 * @author drickert
 *
 */
public class FaceTask implements ITask {

	public static final String UNKNOWN = "unknown";
	@Option(name = "--host", usage = "Host of the SSP (ip or domain)")
	private String SSP_HOST = "141.83.151.196";

	@Option(name = "--port", usage = "Port of the SSP")
	private int SSP_PORT = 5683;

	private String execCode = "";

	public FaceTask() {
		super();
	}

	@Override
	public void run(Configuration config) throws Throwable {
		SSP_HOST = config.getSSP_HOST();
		SSP_PORT = config.getSSP_PORT();
		try {
			// create an observable server
			new Server(this);
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
		Server(FaceTask task) throws IllegalArgumentException, NoSuchFieldException, SecurityException,
				IllegalAccessException, URISyntaxException {
			super();
			ObservableFaceService webresource = new ObservableFaceService("/face", 5, this.getExecutor());
			// start the getFaces Thread, that reads the current faces from
			// System.in
			getFaces(webresource);
			registerWebresource(webresource);

			// register at SSP
			registerAtSSP();

		}

		void getFaces(ObservableFaceService webresource) {
			final String UNKNOWN_FACE = "<Unknown>";
			// set default face
			webresource.setFace(UNKNOWN);
			// reading should be done in an own thread
			new Thread(() -> {
				try {
					// create a scanner to read data
					Scanner scanner = new Scanner(System.in);
					while (true) {
						try {
							System.out.println("new face?");
							// is a new face present?
							if (scanner.hasNextLine()) {
								String newFace = scanner.nextLine();
								// is the face unknown?
								if (UNKNOWN_FACE.equals(newFace)) {
									// set the face to UNKNOWN-Constant
									newFace = UNKNOWN;
								}
								System.out.println("new face is: " + newFace);
								// tell resource about the new face
								webresource.setFace(newFace);
							} else {
								System.out.println("no input available!");
							}
							// wait some time
							Thread.sleep(1000);
						} catch (Exception e) {
							System.out.println(e.getMessage());
							e.printStackTrace();
						}

					}
				} catch (Throwable e1) {
					e1.printStackTrace();
					return;
				}
			}).start();

		}

		/**
		 * Register this endpoint at ssp
		 * 
		 * @throws URISyntaxException
		 */
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
