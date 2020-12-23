package com.frc1678.subsystems;

import com.frc1678.Constants;
import com.frc1678.loops.ILooper;
import com.frc1678.loops.Loop;

import com.team254.lib.drivers.TalonSRXFactory;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

public class Magazine extends Subsystem {
    // instance of this class
    public static Magazine mInstance;

    public static Magazine getInstance() {
        if (mInstance == null) {
            mInstance = new Magazine();
        }
        return mInstance;
    }

    public enum WantedAction {
        UPPER_IDLE, UPPER_FORWARD, UPPER_BACKWARD, 
        LOWER_IDLE, LOWER_FORWARD, LOWER_BACKWARD, 
        SIDE_IDLE, SIDE_PULL_IN, SIDE_AGITATE,
        EXTEND_SIDE_MAGAZINE, EXTEND_FRONT_MAGAZINE,
    }

    public enum UpperState {
        IDLE, FORWARD, BACKWARD, 
    }

    public enum LowerState {
        IDLE, FORWARD, BACKWARD,
    }  

    public enum SideState {
        IDLE, PULL_IN, AGITATE,
    }

    public enum ExtendedState {
        IDLE, SIDE_MAGAZINE_EXTENDED, FRONT_MAGAZINE_EXTENDED,
    }

    private UpperState mUpperState = UpperState.IDLE;
    private LowerState mLowerState = LowerState.IDLE;
    private SideState mSideState = SideState.IDLE;
    private ExtendedState mExtendedState = ExtendedState.IDLE;

    private static PeriodicIO mPeriodicIO = new PeriodicIO();
    public static class PeriodicIO {
        // INPUT
        private double timestamp;

        // OUTPUT
        public boolean side_magazine_extended;
        public boolean front_magazine_extended;
        public double upper_voltage;
        public double side_voltage;
        public double lower_voltage;
    }

    private boolean mCurrentSpiked;
    private boolean mSideConveyorRunning;
    private boolean mUpperConveyorRunning;
    private boolean mLowerConveyorRunning;

    private final TalonSRX mUpperMaster;
    private final TalonSRX mLowerMaster;
    private final TalonSRX mLowerSlave;
    private final TalonSRX mSideMaster;
    private final TalonSRX mSideSlave;

    private final Solenoid mFrontSolenoid;
    private final Solenoid mSideSolenoid;
    
    private Magazine() {
        mFrontSolenoid = Constants.makeSolenoidForId(Constants.kMagazineFrontSolenoidId);
        mSideSolenoid = Constants.makeSolenoidForId(Constants.kMagazineSideSolenoidId);

        mUpperMaster = TalonSRXFactory.createDefaultTalon(Constants.kMagazineUpperMaterId);
        mUpperMaster.set(ControlMode.PercentOutput, 0);
        mLowerMaster = TalonSRXFactory.createDefaultTalon(Constants.kMagazineLowerMaterId);
        mLowerMaster.set(ControlMode.PercentOutput, 0);
        mSideMaster = TalonSRXFactory.createDefaultTalon(Constants.kMagazineSideMaterId);
        mSideMaster.set(ControlMode.PercentOutput, 0);

        mLowerSlave = TalonSRXFactory.createPermanentSlaveTalon(Constants.kMagazineLowerSlaveId, Constants.kMagazineLowerMaterId);
        mLowerSlave.follow(mLowerMaster);
        mSideSlave = TalonSRXFactory.createPermanentSlaveTalon(Constants.kMagazineSideSlaveId, Constants.kMagazineSideMaterId);
        mSideSlave.follow(mSideMaster);
    }

    public void runstateMachine() {
        switch (mUpperState) {
            case IDLE:
                mPeriodicIO.upper_voltage = 0.0;
                mUpperConveyorRunning = false;
                break;
            case FORWARD:
                mPeriodicIO.upper_voltage = 12.0;
                mUpperConveyorRunning = true;
                break;
            case BACKWARD:
                mPeriodicIO.upper_voltage = -4.0;
                mUpperConveyorRunning = true;
                break;
        }

        switch (mLowerState) {
            case IDLE:
                mPeriodicIO.lower_voltage = 0.0;
                mLowerConveyorRunning = false;
                break;
            case FORWARD:
                mPeriodicIO.lower_voltage = 12.0;
                mLowerConveyorRunning = true;
                break;
            case BACKWARD:
            mPeriodicIO.lower_voltage = -12.0;
                mLowerConveyorRunning = true;
                break;
        }

        switch (mSideState) {
            case IDLE:
                mPeriodicIO.side_voltage = 0.0;
                mSideConveyorRunning = false;
                break;
            case PULL_IN:
                mPeriodicIO.side_voltage = 6.0;
                mSideConveyorRunning = true;
                break;
            case AGITATE:
                mPeriodicIO.side_voltage = -6.0;
                mSideConveyorRunning = true;
                break;
        }

        switch (mExtendedState) {
            case IDLE:
                mPeriodicIO.side_magazine_extended = false; 
                mPeriodicIO.front_magazine_extended = false; 
                break;       
            case SIDE_MAGAZINE_EXTENDED:
                mPeriodicIO.side_magazine_extended = true;
                break;
            case FRONT_MAGAZINE_EXTENDED:
                mPeriodicIO.front_magazine_extended = true;
            break;
        }
    }

    public void setState(WantedAction wanted_state) {
        switch (wanted_state) {
            case UPPER_IDLE:
                mUpperState = UpperState.IDLE;
                break;
            case UPPER_FORWARD:
                mUpperState = UpperState.FORWARD;
                break;
            case UPPER_BACKWARD: 
                mUpperState = UpperState.BACKWARD;
                break;
            case LOWER_IDLE:
                mLowerState = LowerState.IDLE;
                break;
            case LOWER_FORWARD:
                mLowerState = LowerState.FORWARD;
                break;
            case LOWER_BACKWARD: 
                mLowerState = LowerState.BACKWARD;
                break;
            case SIDE_IDLE:
                mSideState = SideState.IDLE;
                break;
            case SIDE_PULL_IN:
                mSideState = SideState.PULL_IN;
                break;
            case SIDE_AGITATE:
                mSideState = SideState.AGITATE;
                break;
            case EXTEND_FRONT_MAGAZINE:
                mExtendedState= ExtendedState.FRONT_MAGAZINE_EXTENDED;
                break;
            case EXTEND_SIDE_MAGAZINE:
                mExtendedState = ExtendedState.SIDE_MAGAZINE_EXTENDED;
                break;
        }
    }

    public void writePeriodicOutputs() {
        mFrontSolenoid.set(mPeriodicIO.front_magazine_extended);
        mSideSolenoid.set(mPeriodicIO.side_magazine_extended);

        mUpperMaster.set(ControlMode.PercentOutput, mPeriodicIO.upper_voltage);
        mLowerMaster.set(ControlMode.PercentOutput, mPeriodicIO.lower_voltage);
        mSideMaster.set(ControlMode.PercentOutput, mPeriodicIO.side_voltage);
    }
    public void readPeriodicInputs() {
        mPeriodicIO.timestamp = Timer.getFPGATimestamp();
    }

    public void registerEnabledLoops(ILooper enabledLooper) {
        enabledLooper.register(new Loop() {
            @Override
            public void onStart(double timestamp) {
                mUpperState = UpperState.IDLE;
                mLowerState = LowerState.IDLE;
                mSideState = SideState.IDLE;
                mExtendedState = ExtendedState.IDLE;
            }

            @Override
            public void onLoop(double timestamp) {
                synchronized (Magazine.this) {
                    runstateMachine();
                }
            }

            @Override
            public void onStop(double timestamp) {
                stop();
            }
        });
    }
    public void zeroSensors() {}

    public void stop() {
        mUpperState = UpperState.IDLE;
        mLowerState = LowerState.IDLE;
        mSideState = SideState.IDLE;
        mExtendedState = ExtendedState.IDLE;
    }

    public boolean checkSystem() {
        return true;
    }
}