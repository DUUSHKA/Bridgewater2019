package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSink;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {
	// Elevator
	// left is master, right is slave
	WPI_TalonSRX leftElevator, rightElevator;
	Timer timer;

	 // Intake WPI_VictorSPX intakeTop, intakeBottom;
double range1, range2;
boolean leftGood, rightGood;
	// Drive
	Ultrasonic sensorLeft, sensorRight;
	WPI_TalonSRX leftDrive, rightDrive;
	WPI_VictorSPX leftFollower1, leftFollower2, rightFollower1, rightFollower2;
	SpeedControllerGroup left, right;
	DifferentialDrive drive;
	double throttle;
	double auxThrottle;
	AHRS navx;
	int test;
	int test2;
	// Vision
	UsbCamera camera;
	VideoSink server;

	// Hatch Panel
	Solenoid armSolenoid, armSolenoid1;
	Timer armTimer;
	int armPosition;
	 Compressor compressor;
	 AnalogInput pressureSensor;
	 //Notifier notifier;


	// Joystick
	Joystick joy, aux;

	// Ball Tracking
	Thread t;
	Object o;
	double centerX, centerY, height, width, count;

	// Line Following
	private double rightAccum, leftAccum;
	private int counter;
	private boolean isWhiteFrontLeft, isWhiteFrontRight, isWhiteFrontCenter;
	AnalogInput frontCenter, frontRight, frontLeft;
	public boolean allOff = true;


	@Override
	public void robotInit() {
		joyStickInIt();
		driveInIt();
	//	lineFollowingInIt();
		elevatorInIt();
		// intakeInIt();
		hatchInIt();
		visionInIt();
	//	ultraSonicSensor();
	}

	@Override	public void autonomousInit() {
		armSolenoid.set(true);
		armSolenoid1.set(true);
	}

	@Override
	public void autonomousPeriodic() {
		drive();
		elevator();
//		lineFollowing();
		// intake();
		hatch();
		// ballTracking();
	}
	@Override
	public void teleopPeriodic() {
	//	lineFollowing();
		drive();
		elevator();
		// intake();
		hatch();
	//	autoAlign();
	}

	@Override
	public void testPeriodic() {

	}

	

	public void joyStickInIt() {
		joy = new Joystick(Constants.driveJoystickPort);
		aux = new Joystick(Constants.auxJoyStickPort);
	}

	public void driveInIt() {
		leftDrive = new WPI_TalonSRX(Constants.leftDriveTalonPort);
		leftFollower1 = new WPI_VictorSPX(Constants.leftFollower1Port);
		leftFollower2 = new WPI_VictorSPX(Constants.leftFollower2Port);
		rightDrive = new WPI_TalonSRX(Constants.rightDriveTalonPort);
		rightFollower1 = new WPI_VictorSPX(Constants.rightFollower1Port);
		rightFollower2 = new WPI_VictorSPX(Constants.rightFollower2Port);
		left = new SpeedControllerGroup(leftDrive, leftFollower1, leftFollower2);
		right = new SpeedControllerGroup(rightDrive, rightFollower1, rightFollower2);
		left.setInverted(true);
		right.setInverted(true);
		drive = new DifferentialDrive(left, right);
	}

	public void elevatorInIt() {
		leftElevator = new WPI_TalonSRX(Constants.leftElevatorMotorPort);
		leftElevator.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 0);
		test = (int) Math.IEEEremainder(leftElevator.getSensorCollection().getPulseWidthPosition(), 4096);
		if (test < 0) {
			test += 4096;
		}
		// test2 = (int)Math.IEEEremainder(test, 4096);
		System.out.println("TEST:" + test);
		leftElevator.setSelectedSensorPosition(test);
		leftElevator.config_kP(0, Constants.elevatorP, 0);
		leftElevator.config_kI(0, Constants.elevatorI, 0);
		leftElevator.config_kD(0, Constants.elevatorD, 0);
		rightElevator = new WPI_TalonSRX(Constants.rightElevatorMotorPort);
	}

	public void visionInIt() {
		camera = CameraServer.getInstance().startAutomaticCapture();
		server = CameraServer.getInstance().getServer();
		server.setSource(camera);
	}
	/*
	public void ballTrackingInIt() {
		t = new Thread(() -> {
			while (!Thread.interrupted()) {
				synchronized (o) {
					centerX = SmartDashboard.getNumber("X", 0);
					centerY = SmartDashboard.getNumber("Y", 0);
					width = SmartDashboard.getNumber("WIDTH", 0);
					height = SmartDashboard.getNumber("HEIGHT", 0);
					count = SmartDashboard.getNumber("Count", 0);
				}
			}
		});
		t.start();
	}
	*/
	/*
	 * public void intakeInIt() { intakeTop = new
	 * WPI_VictorSPX(Constants.intakeTopMotorPort); intakeBottom = new
	 * WPI_VictorSPX(Constants.intakeBottomMotorPort); }
	 */
	

	public void hatchInIt() {
		timer = new Timer();
		armSolenoid = new Solenoid(Constants.armSolenoidPort);
		armSolenoid1 = new Solenoid(Constants.armSolenoid1Port);
		armTimer = new Timer();
		armTimer.start();
		armPosition = 0;
		compressor = new Compressor(0);
		pressureSensor = new AnalogInput(Constants.pressureSensorPort);
	}
	/*
	public void lineFollowingInIt() {
		AnalogInput.setGlobalSampleRate(62500);
		//frontCenter = new AnalogInput(Constants.frontCenterChannel);
		//frontRight = new AnalogInput(Constants.frontRightChannel);
		//frontLeft = new AnalogInput(Constants.frontLeftChannel);
		
		
	}
	*/
	/*
	public void ultraSonicSensor(){
		sensorLeft = new Ultrasonic(0, 1);
		sensorRight = new Ultrasonic(2,3);
		range1 = sensorLeft.getRangeInches();
		range2 = sensorRight.getRangeInches();
		leftGood = (range1 <= 4);
		rightGood = (range2 <=4);
	}
	*/
	public void drive() {
    throttle = Math.abs((joy.getThrottle() * .5) - .5001);
    auxThrottle = Math.abs((joy.getThrottle() * .5) - .5001);
		if (joy.getRawButton(ButtonMap.driveKill) || aux.getRawButton(ButtonMap.auxKill)) {
			kill();
		} else {
			drive.arcadeDrive((throttle) * joy.getY(), -1 * (throttle) * joy.getTwist());
		}
	}

	// Needs to be fixed 
	public void elevator() {
		auxThrottle = Math.abs((aux.getThrottle() * .5) - .5001);
		SmartDashboard.putNumber("Absolute", leftElevator.getSensorCollection().getPulseWidthPosition());
		SmartDashboard.putNumber("Relative", leftElevator.getSelectedSensorPosition());
		if (aux.getRawButton(ButtonMap.ballHigh)) {
			leftElevator.set(ControlMode.Position, Constants.ballHighEncoderValue);
			rightElevator.follow(leftElevator);
		} else if (aux.getRawButton(ButtonMap.ballMid)) {
			leftElevator.set(ControlMode.Position, Constants.ballMidEncoderValue);
			rightElevator.follow(leftElevator);
		} else if (aux.getRawButton(ButtonMap.ballLow)) {
			leftElevator.set(ControlMode.Position, Constants.ballLowEncoderValue);
			rightElevator.follow(leftElevator);
		} else if (aux.getRawButton(ButtonMap.hatchPanelHigh)) {
			leftElevator.set(ControlMode.Position, Constants.hatchPanelHighEncoderValue);
			rightElevator.follow(leftElevator);
		} else if (aux.getRawButton(ButtonMap.hatchPanelMid)) {
			leftElevator.set(ControlMode.Position, Constants.hatchPanelMidEncoderValue);
			rightElevator.follow(leftElevator);
		} else if (aux.getRawButton(ButtonMap.hatchPanelLow)) {
			leftElevator.set(ControlMode.Position, Constants.hatchPanelLowEncoderValue);
			rightElevator.follow(leftElevator);
		} else if (aux.getY() > .2) {
			leftElevator.set(ControlMode.PercentOutput, -1 * (auxThrottle) * aux.getY());
      rightElevator.follow(leftElevator);
      /*if(leftElevator.getSensorCollection().getPulseWidthPosition() >= 7081){
        //2217 if not going reeally fast for elevator
        if(aux.getY()>0){
          leftElevator.set(0.0);
          rightElevator.set(0.0);
		}
	}
	*/
      }
      /*if(leftElevator.getSensorCollection().getPulseWidthPosition() >= ){
        kill();
      }
      */ //Need to figure out how to not make it go lower but still availible to go up
		 else if (aux.getY() < -.2) {
			leftElevator.set(ControlMode.PercentOutput, -1 * (auxThrottle) * aux.getY());
      rightElevator.follow(leftElevator);
      /*if(leftElevator.getSensorCollection().getPulseWidthPosition() <= -20000){
        if(aux.getY()>0){
          leftElevator.set(0.0);
          rightElevator.set(0.0);
        }
      }
      */
      //Needs to be fixed, max needs to be found
		} else {
			leftElevator.set(ControlMode.PercentOutput, 0);
			rightElevator.set(ControlMode.PercentOutput, 0);
		}
	}
	/*
	 * //Need to add intake code from laptop public void intake() { if
	 * (joy.getRawButton(ButtonMap.intake) && !limitswitch.get()) {
	 * intakeBottom.set(-0.5); intakeTop.set(0.5); } else if
	 * (joy.getRawButton(ButtonMap.shoot) && !limitswitch.get()) {
	 * intakeBottom.set(0.5); intakeTop.set(-0.5); } else if (limitswitch.get()) {
	 * intakeBottom.set(0.0); intakeTop.set(0.0); } else { intakeBottom.set(0.0);
	 * intakeTop.set(0.0); } }
	 */

	public void hatch() {
		// SmartDashboard.putNumber("Pressure Sensor ", pressureSensor.getValue());
		/*
		if(aux.getTrigger() && armTimer.get() > .3){
			if(armSolenoid.get() == true){
				armSolenoid.set(false);
			}
			else{
				armSolenoid.set(true);
			}
			armTimer.reset();
		}
		*/
		
		if ((joy.getTrigger() || aux.getTrigger())  && armTimer.get() > .3) {
			switch (armPosition) {
			case 1:
				armSolenoid1.set(Constants.closeArms);
				armSolenoid.set(Constants.closeArms);
				//timer.start();%
				//if(timer.hasPeriodPassed(0.1)){
				//	armSolenoid1.set(Constants.closeArms);
				//}
				armPosition = 0;
				
				
				break;
			case 0:
				armSolenoid1.set(Constants.openArms);
				armSolenoid.set(Constants.openArms);
				//timer.start();
				//if(timer.hasPeriodPassed(0.05)){
				
					//timer.stop();
					//timer.reset();
				//}
				armPosition++;
				break;
			} 
			
			armTimer.reset();
 
 
		}
	
	}
	/*
	 * public void ballTracking() { double centerX, centerY, height, width, count;
	 * double scaledR, scaledX; double pGainX = Constants.pGainX; double pGainR =
	 * Constants.pGainR; double targetX = Constants.targetX; double targetR =
	 * Constants.targetR; double leftSpeed, rightSpeed; synchronized (o) { count =
	 * this.count; centerX = this.centerX; centerY = this.centerY; height =
	 * this.height; width = this.width; }
	 * 
	 * scaledR = ((width - targetR) / 316) * pGainR; scaledX = ((centerX - targetX)
	 * / 316) * pGainX; leftSpeed = scaledR - scaledX; rightSpeed = scaledR +
	 * scaledX;
	 * 
	 * if (Math.abs(leftSpeed) > 0.7) { leftSpeed = leftSpeed / Math.abs(leftSpeed)
	 * * 0.7; rightSpeed = rightSpeed / Math.abs(leftSpeed); } else if
	 * (Math.abs(rightSpeed) > 0.7) { rightSpeed = rightSpeed / Math.abs(rightSpeed)
	 * * 0.7; leftSpeed = leftSpeed / Math.abs(rightSpeed); }
	 * 
	 * if (joy.getTrigger()) { if (count != 1) { drive.tankDrive(0, 0); } else {
	 * drive.tankDrive(-leftSpeed, -rightSpeed); } } }
	 */

	public void kill() {
		drive.arcadeDrive(0.0, 0.0);
		// intakeTop.set(0.0);
		// intakeBottom.set(0.0);
    leftElevator.set(0.0);
	rightElevator.set(0.0);
	//armSolenoid.set(Constants.closeArms);
	}

	/*
	 * public void vision() { if (joy.getRawButton(ButtonMap.switchView)) { switch
	 * (cameraView) { case 0: server.setSource(camera1); cameraView++; break; case
	 * 1: server.setSource(camera2); break; } } }
	 */
	/*
	public void lineFollowing() {
		SmartDashboard.updateValues();
		//checkSensors();
		if (joy.getRawButton(ButtonMap.lineFollow)) {
			frontWhip();
		} else {
			rightAccum = 0;
			leftAccum = 0;
			counter = 0;
			allOff = true;
			drive();
		}
		SmartDashboardUpdate();
	}
	*/

	/*
	public void autoAlign(){
		double a, b;
		double a = 0;
		double b = 0;
		drive.tankDrive()
		if(joy.getRawButton(4)){
			if(leftGood = true || rightGood = false){

			}

		}
		if(joy.getRawButton(4)){
			if(leftGood == true && rightGood == false){
				drive.tankDrive(0, 0.3);
			}
			else if(leftGood == false && rightGood == true){
				drive.tankDrive(0.3,0);
			}
			else{
				drive.tankDrive(0,0);
			}
		}
	}
	*/
	public void SmartDashboardUpdate() {
		//SmartDashboard.putBoolean("Touch tape",
			//	frontCenter.getValue() < Constants.sensor_Max && frontCenter.getValue() > Constants.sensor_Min);
	//	SmartDashboard.putNumber("Front Left Sensor", frontLeft.getValue());
	//	SmartDashboard.putNumber("Front Center Sensor:", frontCenter.getValue());
	//	SmartDashboard.putNumber("Front Right Sensor:", frontRight.getValue());
	//	SmartDashboard.putBoolean("isFrontLeftWhite", isWhiteFrontLeft);
	//	SmartDashboard.putBoolean("isFrontRightWhite", isWhiteFrontRight);
	//	SmartDashboard.putBoolean("isFrontCenterWhite", isWhiteFrontCenter);
    SmartDashboard.putNumber("throttle", joy.getThrottle());
    SmartDashboard.putNumber("Throttle AUX", ((0.5*aux.getThrottle()-0.5001)*100)*-1);
	SmartDashboard.putNumber("Aux", aux.getY());
	SmartDashboard.putBoolean("leftGood:", leftGood);
	SmartDashboard.putBoolean("rightGood:", rightGood);
	SmartDashboard.putNumber("test1", sensorLeft.getRangeInches());
	SmartDashboard.putNumber("test2", sensorRight.getRangeInches());
	}
/*	public void checkSensors() {
		if (frontCenter.getValue() < Constants.sensor_Max) {
			isWhiteFrontCenter = true;
		} else {
			isWhiteFrontCenter = false;
		}
		if (frontRight.getValue() < Constants.sensor_Max)
			isWhiteFrontRight = true;
		else {
			isWhiteFrontRight = false;
		}
		if (frontLeft.getValue() < 3000) {
			isWhiteFrontLeft = true;
		} else {
			isWhiteFrontLeft = false;
		}

	}
*/
/*
	public void  frontAutoAlign() {
		drive.tankDrive(-0.3 + rightAccum, -0.3 + leftAccum);
		if (isWhiteFrontLeft && isWhiteFrontRight && isWhiteFrontCenter) {
			leftAccum = 0;
			rightAccum = 0;
		} else if (isWhiteFrontCenter && isWhiteFrontLeft) {
			leftAccum = -0.25;
			rightAccum = -0.25;
		} else if (isWhiteFrontCenter && isWhiteFrontRight) {
			leftAccum = -0.25;
			rightAccum = -0.25;
		} else if (isWhiteFrontLeft) {
			leftAccum = 0;
			rightAccum -= .01;
		} else if (isWhiteFrontRight) {
			rightAccum = 0;
			leftAccum -= .01;
		} else if (isWhiteFrontCenter) {
			leftAccum = 0;
			rightAccum = 0;
		}
		SmartDashboard.putNumber("RightAccum: ", rightAccum);
		SmartDashboard.putNumber("LeftAccum: ", leftAccum);
		//checkSensors();
	}

	public void frontWhip() {
		//checkSensors();
			if (allOff && !isWhiteFrontLeft && !isWhiteFrontCenter && !isWhiteFrontRight && counter < 18) {
			drive.tankDrive(0.6, -0.6);
			counter++;
		} else if (allOff && !isWhiteFrontLeft && !isWhiteFrontCenter && !isWhiteFrontRight && counter < 54) {
			drive.tankDrive(-0.6, 0.6);
			counter++;
		} else {
			allOff = false;
		}
		if (!allOff) {
			System.out.println(counter);
			System.out.println("working");
			drive.tankDrive(0, 0);
			frontAutoAlign();
		}
	}
*/
}