package com.frc1678;

import edu.wpi.first.wpilibj.Solenoid;

public class Constants {

	public static double kLooperDt;

    // ball Intake Variables
    public static double kBallIntakingVoltage = 10.0;
    public static double kBallOuttakingVoltage = -10.0;
    public static double kBallSlowIntakingVoltage = -10.0;
	public static double kBallIdleVoltage = 0.0;

    public static int kBallIntakeMasterId;
    public static int kBallIntakeSlaveId;

	public static int kBallIntakeSolenoidId = 1;

    // gear intake variables
    public static double kGearIntakingVoltage = 12.0;  
    public static double kGearPickupVoltage = 2.5;
    public static double kGearCarryingVoltage = 1.5;
	public static double kGearScoringVoltage = -12.0;
    public static double kGearOuttakingVoltage = -4.0;
    
    public static int kGearPickupTicks = 300;
    public static double kGearCurrentThreshhold = 60.0;

    public static int kGearIntakeMotorId = 2;
    public static int kGearIntakeSolenoidId = 2;

    // magazine variables
    public static int kMagazineUpperMaterId = 3;
    public static int kMagazineLowerMaterId = 3;
    public static int kMagazineSideMaterId = 3;
    public static int kMagazineLowerSlaveId = 4;
    public static int kMagazineSideSlaveId = 4;

	public static int kMagazineFrontSolenoidId;
    public static int kMagazineSideSolenoidId;
    
    // shooter variables
	public static int kShooterMasterId;
	public static int kShooterSlave1Id;
	public static int kShooterSlave2Id;
	public static int kShooterSlave3Id;

    public static double kVelocityConversion = 600.0/2048.0;

    // solenoids
    public static final int kPCMId = 20;
    public static final int kPDPId = 21;

    public static Solenoid makeSolenoidForId(int solenoidId) {
        if (solenoidId < 8) {
            return new Solenoid(kPCMId, solenoidId);
        }
        throw new IllegalArgumentException("Solenoid ID not valid: " + solenoidId);
    }
    
}