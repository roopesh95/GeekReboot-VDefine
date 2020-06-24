package application;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javafx.scene.control.ProgressIndicator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import org.apache.velocity.app.VelocityEngine;

import application.FaceDetector;
import application.Database;
import application.Database;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SampleController {

	public String filePath = "./faces";

	@FXML
	private Button startCam;
	@FXML
	private Button stopBtn;
	
	@FXML
	private Button saveBtn;
	
	@FXML
	private Button recogniseBtn;
	@FXML
	private Button stopRecBtn;
	@FXML
	private ImageView frame;
	@FXML
	private ImageView motionView;
	@FXML
	private AnchorPane pdPane;
	@FXML
	private TitledPane dataPane;
	@FXML
	private TextField fname;
	@FXML
	private TextField lname;
	@FXML
	private TextField code;
	@FXML
	private TextField reg;
	@FXML
	private TextField sec;
	@FXML
	private TextField age;
	@FXML
	public ListView<String> logList;
	@FXML
	public ListView<String> output;
	@FXML
	public ProgressIndicator pb;
	@FXML
	public Label savedLabel;
	@FXML
	public Label warning;
	@FXML
	public Label title;
	@FXML
	public TilePane tile;
	@FXML
	public TextFlow ocr;

	FaceDetector faceDetect = new FaceDetector(); // Creating Face detector object

	Database database = new Database(); // Creating Database object

	SensorData sensorData = new SensorData();

	VelocityEngine velocityEngine = new VelocityEngine();

	ArrayList<String> user = new ArrayList<String>();
	ImageView imageView1;

	public static ObservableList<String> event = FXCollections.observableArrayList();
	public static ObservableList<String> outEvent = FXCollections.observableArrayList();

	public boolean enabled = false;
	public boolean isDBready = false;

	public void putOnLog(String data) {

		Instant now = Instant.now();

		String logs = now.toString() + ":\n" + data;

		event.add(logs);
		System.out.println(logs);

		// logList.setItems(event);

	}

	@FXML
	protected void startCamera() throws SQLException {

		faceDetect.init();

		faceDetect.setFrame(frame);

		faceDetect.start();

		if (!database.init()) {

			putOnLog("Error: Database Connection Failed ! ");

		} else {
			isDBready = true;
			putOnLog("Success: Database Connection Succesful ! ");
		}

		// Activating other buttons
		startCam.setVisible(false);

		stopBtn.setVisible(true);
		// ocrBtn.setDisable(false);
		saveBtn.setDisable(false);

		if (isDBready) {
			recogniseBtn.setDisable(false);
		}

		dataPane.setDisable(false);

		if (tile != null) {
			tile.setPadding(new Insets(15, 15, 55, 15));
			tile.setHgap(30);
		}

		// Picture Gallary

		String path = filePath;

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		// Image reader from the mentioned folder
		for (final File file : listOfFiles) {

			imageView1 = createImageView(file);
			if (tile != null) {
				tile.getChildren().addAll(imageView1);
			}

		}
		putOnLog(" Real Time WebCam Stream Started !");

	}

	int count = 0;

	@FXML
	protected void faceRecognise() throws InterruptedException {

		faceDetect.setIsRecFace(true);
		// recogniseBtn.setText("Get Face Data");

		// Getting detected faces
		user = faceDetect.getOutput();

		if (count > 0 && user.size() > 0) {
			String[] arr = { "97.5", "97.8", "98.1", "98.6", "100.5", "100.9", "100.7", "101.3", "102.2", "103.4" };
			Random rand = new Random();
			int randomNumber = rand.nextInt(arr.length);
			System.out.println(arr[randomNumber]);
			Calendar cal = Calendar.getInstance();
			Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
			database.loginTime = timestamp;
			
			//TO get Data from sensor
			// database.temperature = Float.parseFloat(sensorData.getTemperatureData());
			
			//To get temperature random temperature
			database.temperature = Float.parseFloat(arr[randomNumber]);
			
			database.insertAttendence(user);
			if (database.temperature >= 100) {
				sendMail(user);
			}

			// Retrieved data will be shown in Fetched Data pane
			String t = "********* Face Data *********";

			outEvent.add(t);

			String n1 = "First Name\t\t:\t" + user.get(1);

			outEvent.add(n1);

			output.setItems(outEvent);

			String n2 = "Last Name\t\t:\t" + user.get(2);

			outEvent.add(n2);

			output.setItems(outEvent);

			/*
			 * String fc = "Face Code\t\t:\t" + user.get(0);
			 * 
			 * outEvent.add(fc);
			 * 
			 * output.setItems(outEvent);
			 */
			String r = "Employee ID\t\t:\t" + user.get(3);

			outEvent.add(r);

			output.setItems(outEvent);

			String a = "Age \t\t\t\t:\t" + user.get(4);

			outEvent.add(a);

			output.setItems(outEvent);
			String s = "Department\t\t:\t" + user.get(5);
			outEvent.add(s);

			output.setItems(outEvent);
			String te = "Temperature\t\t:\t" + database.temperature + " °F";

			outEvent.add(te);

			output.setItems(outEvent);
			String l = "Login Time\t\t:\t" + database.getLoginTime();

			outEvent.add(l);

			output.setItems(outEvent);
		}

		count++;

		putOnLog("Face Recognition Activated !");
		if (user.size() == 0) {

			Thread.sleep(1000);
			faceRecognise();

		} else {

			return;
		}

	}

	@FXML
	protected void stopRecognise() {

		faceDetect.setIsRecFace(false);
		faceDetect.clearOutput();
		this.user.clear();

		recogniseBtn.setText("Recognise Face");

		stopRecBtn.setDisable(true);

		putOnLog("Face Recognition Deactivated !");

	}

	@FXML
	protected void saveFace() throws SQLException {

		// Input Validation
		if (fname.getText().trim().isEmpty() || reg.getText().trim().isEmpty() ) {

			new Thread(() -> {

				try {
					warning.setVisible(true);

					Thread.sleep(2000);

					warning.setVisible(false);

				} catch (InterruptedException ex) {
				}

			}).start();

		} else {
			// Progressbar
			pb.setVisible(true);

			savedLabel.setVisible(true);

			new Thread(() -> {

				try {
					 Random rand = new Random(); 
				     int rand_int1 = rand.nextInt(1000); 
					faceDetect.setFname(fname.getText());

					faceDetect.setFname(fname.getText());
					faceDetect.setLname(lname.getText());
					faceDetect.setAge(Integer.parseInt(age.getText()));
					faceDetect.setCode(rand_int1);
					faceDetect.setSec(sec.getText());
					faceDetect.setReg(Integer.parseInt(reg.getText()));

					database.setFname(fname.getText());
					database.setLname(lname.getText());
					database.setAge(Integer.parseInt(age.getText()));
					database.setCode(rand_int1);
					database.setSec(sec.getText());
					database.setReg(Integer.parseInt(reg.getText()));

					database.insert();
					lname.setText("");
					age.setText("");
					sec.setText("");
					reg.setText("");
					fname.setText("");
					javafx.application.Platform.runLater(new Runnable() {

						@Override
						public void run() {
							pb.setProgress(100);
						}
					});

					savedLabel.setVisible(true);
					Thread.sleep(2000);

					javafx.application.Platform.runLater(new Runnable() {

						@Override
						public void run() {
							pb.setVisible(false);
						}
					});

					javafx.application.Platform.runLater(new Runnable() {

						@Override
						public void run() {
							savedLabel.setVisible(false);
						}
					});

				} catch (InterruptedException ex) {
				}

			}).start();

			faceDetect.setSaveFace(true);

		}

	}

	@FXML
	protected void stopCam() throws SQLException {

		faceDetect.stop();

		startCam.setVisible(true);
		stopBtn.setVisible(false);

		/* this.saveFace=true; */

		putOnLog("Cam Stream Stopped!");

		recogniseBtn.setDisable(true);
		saveBtn.setDisable(true);
		faceDetect.clearOutput();
		// stopRecBtn.setDisable(true);


		database.db_close();
		putOnLog("Database Connection Closed");
		isDBready = false;
	}

	private ImageView createImageView(final File imageFile) {

		try {
			final Image img = new Image(new FileInputStream(imageFile), 120, 0, true, true);
			imageView1 = new ImageView(img);

			imageView1.setStyle("-fx-background-color: BLACK");
			imageView1.setFitHeight(120);

			imageView1.setPreserveRatio(true);
			imageView1.setSmooth(true);
			imageView1.setCache(true);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return imageView1;
	}

	public void sendMail(ArrayList<String> user) {

		String to = "";

		// Sender's email ID needs to be mentioned
		String from = "";

		// Assuming you are sending email from localhost
		String host = "smtp.gmail.com";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.socketFactory.port", 465);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.port", "465");    
		

		final String username = "";// change accordingly
		final String password = "";
		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject: header field
			message.setSubject("ALERT");

			// Now set the actual message
			message.setText("Employee ID: " + user.get(3)
					+ "has recorded temperature more than the permissible limit. Please take necessary action");

			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....");
		} catch (MessagingException mex) {
			System.out.println("Error:" + mex);
			mex.printStackTrace();
		}

	}

}
