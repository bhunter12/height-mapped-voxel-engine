package main.java.com.lazymachine.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class ScreenshotRepository {

    public ByteBuffer getScreenShot(int xScreenResolution, int yScreenResolution, int bitsPerPixel) {
        GL11.glReadBuffer(GL11.GL_FRONT); 
        int width = xScreenResolution;
        int height = yScreenResolution;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bitsPerPixel);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );

        return buffer;
    }
    
    public void saveScreenShot(ByteBuffer buffer, String verticalLineTestPathAndFilename, int xScreenResolution, int yScreenResolution,
                               int bitsPerPixel) {
        File file = new File(verticalLineTestPathAndFilename);
        String format = "PNG";
        BufferedImage image = new BufferedImage(xScreenResolution, yScreenResolution, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < xScreenResolution; x++) {
            for(int y = 0; y < yScreenResolution; y++)
            {
                int i = (x + (xScreenResolution * y)) * bitsPerPixel;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, yScreenResolution - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
}
