package com.frc1678.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.frc1678.Constants;
import com.frc1678.loops.ILooper;
import com.team254.lib.drivers.TalonSRXFactory;

import edu.wpi.first.wpilibj.Timer;

public class Shooter extends Subsystem {
    // instance of this class
    public static Shooter mInstance;

    public static Shooter getInstance() {
        if (mInstance == null) {
            mInstance = new Shooter();
        }
        return mInstance;
    }

    private static PeriodicIO mPeriodicIO = new PeriodicIO();

    public static class PeriodicIO {
        // INPUT
        private double timstamp;
        private double shooter_encoder_position;
        private double accelerator_encoder_position;
        private double observed_velocity;
        private double observed_voltage;
        private double observed_current;
        private double observed_temperature;

        // OUTPUT
        // velocity
        private double demand;

    }
    private final TalonSRX mMaster;
    private final TalonSRX mSlave1;
    private final TalonSRX mSlave2;
    private final TalonSRX mSlave3;

    private Shooter() {
        mMaster = TalonSRXFactory.createDefaultTalon(Constants.kShooterMasterId);
        mMaster.set(ControlMode.PercentOutput, 0.0);
        mSlave1 = TalonSRXFactory.createPermanentSlaveTalon(Constants.kShooterSlave1Id, Constants.kShooterMasterId);
        mMaster.set(ControlMode.PercentOutput, 0.0);
        mSlave2 = TalonSRXFactory.createPermanentSlaveTalon(Constants.kShooterSlave2Id, Constants.kShooterMasterId);
        mMaster.set(ControlMode.PercentOutput, 0.0);
        mSlave3 = TalonSRXFactory.createPermanentSlaveTalon(Constants.kShooterSlave3Id, Constants.kShooterMasterId);
        mSlave1.follow(mMaster);
        mSlave2.follow(mMaster);
        mSlave3.follow(mMaster);

    }

    public synchronized void setVelocity(double velocity) {
        mPeriodicIO.demand =  velocity;
    }

    public synchronized double getVelocity() {
        return mPeriodicIO.observed_velocity;
    }

    public synchronized boolean spunUp() {
        if (mPeriodicIO.observed_velocity > 0.0) {
            return true;
        }
        return false;
    }

    // TODO: state-space? 
    public synchronized boolean atGoal() {
        if (mPeriodicIO.observed_velocity == mPeriodicIO.demand) {
            return true;
        }
        return false;
    }

    public void writePeriodicOutputs() {
        mMaster.set(ControlMode.Velocity, mPeriodicIO.demand / Constants.kVelocityConversion);
    }

    public void readPeriodicInputs() {
        mPeriodicIO.timstamp = Timer.getFPGATimestamp();
        mPeriodicIO.observed_velocity = mMaster.getSelectedSensorVelocity() * Constants.kVelocityConversion;
        mPeriodicIO.observed_voltage = mMaster.getMotorOutputVoltage();
        mPeriodicIO.observed_current = mMaster.getStatorCurrent();
        mPeriodicIO.observed_temperature = mMaster.getTemperature();
    }

    public void registerEnabledLoops(final ILooper mEnabledLooper) {}
    public void zeroSensors() {}
    public void stop() {

    }
    public boolean checkSystem() {
        return true;
    }
}