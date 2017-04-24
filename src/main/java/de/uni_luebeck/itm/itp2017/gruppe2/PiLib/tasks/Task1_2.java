package de.uni_luebeck.itm.itp2017.gruppe2.PiLib.tasks;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Task1_2 {

	public static void main(String[] args) throws InterruptedException {
		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		final GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
		GpioPinDigitalInput ldr = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29);

		ldr.addListener(new GpioPinListenerDigital() {

			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				// display pin state on console
				if(event.getState().compareTo(PinState.HIGH) == 0) {
					led.setState(PinState.HIGH);
				} else {
					led.setState(PinState.LOW);
				}
			}

		});

		while(true)
			Thread.sleep(5000);
	}
	
}
