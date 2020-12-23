package com.frc1678.subsystems;

import com.frc1678.Constants;
import com.frc1678.loops.ILooper;
import com.frc1678.loops.Loop;

import com.team254.lib.drivers.TalonSRXFactory;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;

public class GearIntake extends Subsystem {
    // instance of this class
    public static GearIntake mInstance;

    public synchronized static GearIntake getInstance() {
        if (mInstance == null) {
            mInstance = new GearIntake();
        }
        return mInstance;
    }

    public enum WantedAction {
        NONE, DROP, RISE, SCORE, OUTTAKE, START_DROPPING_BALLS, STOP_DROPPING_BALLS,
    }

    public enum State {
        IDLE, INTAKING, PICKING_UP, CARRYING, SCORING, OUTTAKING, DROP_BALL_WITH_GEAR, DROP_BALL_WITHOUT_GEAR,
    }

    private State mState = State.IDLE;

    private static PeriodicIO mPeriodicIO = new PeriodicIO();
    public static class PeriodicIO {
        // INPUT
        public double timestamp;
        public double current;

        // OUTPUT
        public double demand;
        public boolean intake_down;
    }

    private boolean mCurrentSpiked = false;
    private int mPickupTimer = 0;

    private final Solenoid mSoleniod;
    private final TalonSRX mMaster;

    private GearIntake() {
        mSoleniod = Constants.makeSolenoidForId(Constants.kGearIntakeSolenoidId);
        mMaster = TalonSRXFactory.createDefaultTalon(Constants.kGearIntakeMotorId);
        mMaster.set(ControlMode.PercentOutput, 0);
    }

    @Override
    public void writePeriodicOutputs() {
        mSoleniod.set(mPeriodicIO.intake_down);
        if (mPeriodicIO.intake_down) {
            mMaster.set(ControlMode.PercentOutput, mPeriodicIO.demand);
        }
        else {
            mMaster.set(ControlMode.PercentOutput, 0);
        }
    }

    @Override
    public void readPeriodicInputs() {
        mPeriodicIO.timestamp = Timer.getFPGATimestamp();
    }

    @Override
    public void registerEnabledLoops(ILooper enabledLooper) {
        enabledLooper.register(new Loop() {
            @Override
            public void onStart(double timestamp) {
                mState = State.IDLE;
            }

            @Override
            public void onLoop(double timestamp) {
                synchronized (GearIntake.this) {
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
        case IDLE:
            mPeriodicIO.demand = 0.0;
            mPeriodicIO.intake_down = false;
            break;
        case INTAKING:
            mPeriodicIO.demand = Constants.kGearIntakingVoltage;
            mPeriodicIO.intake_down = true;
            if (mPeriodicIO.current > Constants.kGearCurrentThreshhold) {
                mCurrentSpiked = true;
                mState = State.PICKING_UP;
                mPickupTimer = Constants.kGearPickupTicks;
                mPeriodicIO.demand = Constants.kGearPickupVoltage;
            }
            break;
        case PICKING_UP:
            mPeriodicIO.demand = Constants.kGearPickupVoltage;
            mPeriodicIO.intake_down = false;
            if (mPickupTimer < 0) {
                mState = State.CARRYING;
            }
            break;
        case CARRYING:
            mPeriodicIO.demand = Constants.kGearCarryingVoltage;
            mPeriodicIO.intake_down = false;
            break;
        case SCORING:
            mPeriodicIO.demand = Constants.kGearScoringVoltage;
            mPeriodicIO.intake_down = false;
            break;
        case OUTTAKING:
            mPeriodicIO.demand = Constants.kGearOuttakingVoltage;
            mPeriodicIO.intake_down = true;
            break;
        case DROP_BALL_WITH_GEAR:
            mPeriodicIO.demand  = 2.5;
            mPeriodicIO.intake_down = true;
            break;
        case DROP_BALL_WITHOUT_GEAR:
            mPeriodicIO.demand = 0;
            mPeriodicIO.intake_down = true;
            break;
        }
    }

    public boolean isCurrentSpiked() {
        return mCurrentSpiked;
    }

    public double getVoltage() {
        return mPeriodicIO.demand;
    }

    public void setState(WantedAction wanted_state) {
        switch (wanted_state) {
            case NONE:
                mState = State.IDLE;
                break;
            case DROP:
                mState = State.INTAKING;
                break;
            case RISE:
                if (mState == State.INTAKING) {
                    mState = State.IDLE;
                }
                break;
            case SCORE:
                if (mState == State.CARRYING || mState == State.IDLE) {
                    mState = State.SCORING;
                }
                break;
            case OUTTAKE:
                mState = State.OUTTAKING;
                break;
            case START_DROPPING_BALLS:
                if (mState == State.PICKING_UP || mState == State.CARRYING) {
                    mState = State.DROP_BALL_WITH_GEAR;
                }
                if (mState == State.INTAKING || mState == State.IDLE) {
                    mState = State.DROP_BALL_WITHOUT_GEAR;
                }
                break;
            case STOP_DROPPING_BALLS:
                if (mState == State.DROP_BALL_WITHOUT_GEAR) {
                    mState = State.IDLE;
                }
                if (mState == State.DROP_BALL_WITH_GEAR) {
                    mState = State.CARRYING;
                }
                break;
        }
    }

    @Override
    public void zeroSensors() {}

    @Override
    public void stop() {
        mPeriodicIO.demand = 0.0;
        mPeriodicIO.intake_down = false;
    }
    @Override
    public boolean checkSystem() {
        return true;
    };

}