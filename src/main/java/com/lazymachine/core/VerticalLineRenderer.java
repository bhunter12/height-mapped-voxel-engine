package main.java.com.lazymachine.core;

import org.lwjgl.opengl.GL11;

public class VerticalLineRenderer {
    
    private LookupTableProvider lookupTableProvider;
    
    public VerticalLineRenderer(int mapXLength, int screenXAxisMax, int angle360, double angularIncrement) {
        lookupTableProvider = new LookupTableProvider(mapXLength, screenXAxisMax, angle360, angularIncrement);
    }

    public void drawVerticalLineSegment(int topYCoordinate, int bottomYCoordinate, int xCoordinate, 
                                         int pixelColor) {
        int temp = 0;

        // My implementation: make sure topY > bottomY
        if (bottomYCoordinate > topYCoordinate) {
            temp = topYCoordinate;
            topYCoordinate = bottomYCoordinate;
            bottomYCoordinate = temp;
        }
        float[] openGlAdjustedPixelColorLookup = lookupTableProvider.openGlAdjustedPixelColorLookup();
        GL11.glColor3f(openGlAdjustedPixelColorLookup[pixelColor], openGlAdjustedPixelColorLookup[pixelColor],
                       openGlAdjustedPixelColorLookup[pixelColor]);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(xCoordinate, topYCoordinate);
        GL11.glVertex2i(xCoordinate, bottomYCoordinate);
        GL11.glEnd();
    }
    
}
