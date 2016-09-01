package main.java.com.lazymachine.core;

import main.java.com.lazymachine.math.Trigonometry;

// TODO: May need to further split this class out
public class LookupTableProvider {

    private int[] rayHitYTextureCoordinateLookup;
    private float[] openGlAdjustedPixelColorLookup;
    private double[] sineLookup;
    private double[] cosineLookup;
    
    public LookupTableProvider(int mapXLength, int screenXAxisMax, int angle360, double angularIncrement) {
        createRayHitYTextureCoordinateLookupTable(mapXLength, screenXAxisMax);
        createOpenGlAdjustedPixelColorLookupTable();
        createSineAndCosineLookupTables(angle360, angularIncrement);
    }

    public int[] rayHitYTextureCoordinateLookupTable() {
        return rayHitYTextureCoordinateLookup;
    }
    
    public float[] openGlAdjustedPixelColorLookup() {
        return openGlAdjustedPixelColorLookup;
    }
    
    public double[] sineLookup() {
        return sineLookup;
    }
    
    public double[] cosineLookup() {
        return cosineLookup;
    }
    
    private void createRayHitYTextureCoordinateLookupTable(int mapXLength, int screenXAxisMax) {
        rayHitYTextureCoordinateLookup = new int[screenXAxisMax];
        for (int i = 0; i < mapXLength; i++) {
            rayHitYTextureCoordinateLookup[i] = i * 1024;
            System.out.println("i: " + i);
        }
    }
    
    private void createOpenGlAdjustedPixelColorLookupTable() {
        openGlAdjustedPixelColorLookup = new float[256];
        for (int i = 0; i < 256; i++) {
            openGlAdjustedPixelColorLookup[i] = i / 255.0f;
        }
    }
    
    private void createSineAndCosineLookupTables(int angle360, double angularIncrement) {
        double currentRadianAngle;
        int angle;
        this.cosineLookup = new double[angle360];
        this.sineLookup = new double[angle360];
        for (angle = 0; angle < angle360; angle++) {
            currentRadianAngle = Trigonometry.computeAngleInRadians(angle, angularIncrement); 
            sineLookup[angle] = Math.sin(currentRadianAngle);
            cosineLookup[angle] = Math.cos(currentRadianAngle);          
        }
    }
    
}
