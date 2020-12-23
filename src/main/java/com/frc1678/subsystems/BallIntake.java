package com.frc1678.subsystems;

import com.frc1678.Constants;
import com.frc1678.loops.ILooper;
import com.frc1678.loops.Loop;

import com.team254.lib.drivers.TalonSRXFactory;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;

public class BallIntake extends Subsystem {
    // instance of this class
    private static BallIntake mInstance;

    public synchronized static BallIntake getInstance() {
        if (mInstance == null) {
            mInstance = new BallIntake();
        }
        return mInstance;
    }

    // ball intake goals
    public enum WantedAction {
        IDLE, INTAKE, OUTTAKE, SLOW_INTAKE,
    }

    // ball intake state 
    public enum State {
        IDLE, INTAKING, OUTTAKING, SLOW_INTAKING,
    }

    private State mState = State.IDLE;

    // periodic outputs
    private static PeriodicIO mPeriodicIO = new PeriodicIO();
    public static class PeriodicIO {
        // INPUTS
        public double timestamp;
        
        // OUTPUTS
        public double demand;
        public boolean intake_down;
    }

    // intake soleniod
    private final Solenoid mSolenoid;
    
    // intake motors
    private final TalonSRX mMaster;
    private final TalonSRX mSlave;

    private BallIntake() {
        mSolenoid = Constants.makeSolenoidForId(Constants.kBallIntakeSolenoidId);
        mMaster = TalonSRXFactory.createDefaultTalon(Constants.kBallIntakeMasterId);
        mMaster.set(ControlMode.PercentOutput, 0);
        mSlave = TalonSRXFactory.createPermanentSlaveTalon(Constants.kBallIntakeSlaveId, Constants.kBallIntakeMasterId);
        mSlave.follow(mMaster);
    }

    @Override
    public void stop() {
        mMaster.set(ControlMode.PercentOutput, 0);  
    }

    @Override
    public void zeroSensors() {}

    @Override
    public void registerEnabledLoops(ILooper enabledLooper) {
        enabledLooper.register(new Loop() {
            @Override
            public void onStart(double timestamp) {
                mState = State.IDLE;
            }

            @Override
            public void onLoop(double timestamp) {
                synchronized (BallIntake.this) {
                    runStateMachine();
                }
            }

            @Override
            public void onStop(double timestamp) {
                mState = State.IDLE;
                stop();
            }
        });
    }

    public synchronized State getState() {
        return mState;
    }

    public void runStateMachine() {
        switch (mState) {
        case INTAKING:
                mPeriodicIO.demand = Constants.kBallIntakingVoltage;
                mPeriodicIO.intake_down = true;
            break;
        case OUTTAKING:
                mPeriodicIO.demand = Constants.kBallOuttakingVoltage;
                mPeriodicIO.intake_down = true;
            break;
        case SLOW_INTAKING:
            mPeriodicIO.demand = Constants.kBallSlowIntakingVoltage;
            mPeriodicIO.intake_down = true;
            break;
        case IDLE:
                mPeriodicIO.demand = Constants.kBallIdleVoltage;
                mPeriodicIO.intake_down = false;
        }
    }

    public double getVoltage() {
        return mPeriodicIO.demand;
    }

    public boolean IsIntakeDown() {
        return mPeriodicIO.intake_down;
    }

    public void setState(WantedAction wanted_state) {
        switch (wanted_state) {
        case INTAKE:
            mState = State.INTAKING;
            break;
        case OUTTAKE:
            mState = State.OUTTAKING;
            break;
        case SLOW_INTAKE:
            mState = State.SLOW_INTAKING;
            break;
        case IDLE:
            mState = State.IDLE;
            break;
        }
    }

    @Override
    public void readPeriodicInputs() {
        mPeriodicIO.timestamp = Timer.getFPGATimestamp();
    }

    @Override
    public void writePeriodicOutputs() {
        mSolenoid.set(mPeriodicIO.intake_down);
        if (mPeriodicIO.intake_down) {
            mMaster.set(ControlMode.PercentOutput, mPeriodicIO.demand / 12.0);
        }
        else {
            mMaster.set(ControlMode.PercentOutput, 0);
        }
    }

    @Override
    public boolean checkSystem() {
        return true;
    }
}