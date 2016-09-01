package main.java.com.lazymachine.math;

public class Trigonometry {
    
    private static final double DEGREES_TO_RADIANS_CONVERSION_CONSTANT = Math.PI / 180;
    
    public static double computeAngleInRadians(int angle, double angularIncrement) {
        double currentRadianAngle;
        currentRadianAngle = angle * angularIncrement * DEGREES_TO_RADIANS_CONVERSION_CONSTANT;
        return currentRadianAngle;
    }
}
