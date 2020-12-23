package com.frc1678.subsystems;

import com.frc1678.loops.ILooper;

public abstract class Subsystem {
    public void writePeriodicOutputs() {}
    public void readPeriodicInputs() {}
    public void registerEnabledLoops(ILooper mEnabledLooper) {}
    public void zeroSensors() {}
    public abstract void stop();
    public abstract boolean checkSystem();
}