package frc.robot;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
public class Constants {
    //Joysticks
    public static final int driveJoystickPort = 0;
    public static final int auxJoyStickPort = 1;

  //Drive
  //left ports are even, right ports are odd
    public static final int leftDriveTalonPort = 1;
    public static final int leftFollower1Port = 3;
    public static final int leftFollower2Port = 5;
    public static final int rightDriveTalonPort = 2;
    public static final int rightFollower1Port = 4;
    public static final int rightFollower2Port = 6;

    //Ball Tracking
    public static final double pGainX = 1;
    public static final double pGainR = 1.5;
    public static final double targetX = 170;
    public static final double targetR = 270;
    
    //Arms(PCM)
    public static final int armSolenoidPort = 1;
    public static final int armSolenoid1Port = 3;
    public static final boolean openArms = true; 
    public static final boolean closeArms = false;
    public static final Value armsOff = Value.kOff;
    public static final int pressureSensorPort = 3;

    //Elevator
    public static final int leftElevatorMotorPort = 7;
    public static final int rightElevatorMotorPort = 8;
    public static final double ballHighEncoderValue = 0;
    public static final double hatchPanelHighEncoderValue = 0;
    public static final double ballMidEncoderValue = 0;
    public static final double hatchPanelMidEncoderValue = 0;
    public static final double ballLowEncoderValue = 0;
    public static final double hatchPanelLowEncoderValue = 0;
    public static final double elevatorP = 0.0;
    public static final double elevatorI = 0.0;
    public static final double elevatorD = 0.0;
    public static final int kTimeoutMs = 10;
    
    /*
    //Intake
    public static final int intakeTopMotorPort = 0;
    public static final int intakeBottomMotorPort = 0;
    public static final int intakeLimitSwitchChannel = 0;
    */

    //Line Following
    public final static double sensor_Min = 1600;
    public final static double sensor_Max = 3250;
    public final static int frontCenterChannel = 1;
    public final static int frontRightChannel = 2;
    public final static int frontLeftChannel = 0;
}