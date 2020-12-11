package com.frc1678.subsystems;

import com.frc1678.Constants;
import com.frc1678.loops.ILooper;
import com.frc1678.loops.Loop;
import com.frc1678.Constants;

import com.team254.lib.drivers.LazyTalonFX;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class BallIntake extends Subsystem {
    private static double kIntakingVoltage = -12.0;
    private static double kIdleVoltage = 0;

    private static BallIntake mInstance;

    private Solenoid mDeploySolenoid;

    public enum WantedAction {
        IDLE, INTAKE, OUTTAKE, SLOW_INTAKE,
    }

    public enum State {
        IDLE, INTAKING, OUTTAKING, SLOW_INTAKING,
    }

    private State mState = State.IDLE;

    private static PeriodicIO mPeriodicIO = new PeriodicIO();

    private final LazyTalonFX mMaster = new LazyTalonFX(1);

    public static class PeriodicIO {
        // INPUTS
        public double timestamp;
        public double current;

        // OUTPUTS
        public double demand;
        public boolean deploy;
    }
    
    public synchronized static BallIntake getInstance() {
        if (mInstance == null) {
            mInstance = new BallIntake();
        }
        return mInstance;
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
                // startLogging();
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
                mPeriodicIO.demand = Constants.kIntakingVoltage;
                mPeriodicIO.deploy = true;
            break;
        case OUTTAKING:
                mPeriodicIO.demand = Constants.kOuttakingVoltage;
                mPeriodicIO.deploy = false;
            break;
        case SLOW_INTAKING:
            mPeriodicIO.demand = Constants.kSlowIntakingVoltage;
            mPeriodicIO.deploy = false;
        case IDLE:
                mPeriodicIO.demand = Constants.kIdleVoltage;
        }
    }

    public synchronized void setOpenLoop(double percentage) {
        mPeriodicIO.demand = percentage;
    }

    public double getVoltage() {
        return mPeriodicIO.demand;
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
    public synchronized void readPeriodicInputs() {}

    @Override
    public void writePeriodicOutputs() {
        mMaster.set(mPeriodicIO.demand / 12.0);
        mDeploySolenoid.set(mPeriodicIO.deploy);
    }

    @Override
    public boolean checkSystem() {
        return true;
    }
}