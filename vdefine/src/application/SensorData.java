package application;

import java.util.Scanner;

import javax.sql.rowset.serial.SerialClob;

import com.fazecast.jSerialComm.SerialPort;

public class SensorData {

	public static String getTemperatureData() {

		SerialPort ports[] = SerialPort.getCommPorts();
		for (SerialPort port : ports) {
			System.out.println(port.getSystemPortName());
		}

		SerialPort port = ports[7];
		SerialPort sp = SerialPort.getCommPort("COM10");
		sp.setComPortParameters(9600, 8, 1, 0);
		sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

		if (sp.openPort()) {

			System.out.println("Port Is Opened");

			Scanner data = new Scanner(sp.getInputStream());
			System.out.println(sp.getInputStream());
			if(data.hasNext()) {
				return data.next();
			}else {
				return null;
			}
		} else {
			System.out.println("Port is not opened");
			return null;
		}

	}

}
