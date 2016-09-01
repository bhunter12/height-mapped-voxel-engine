package main.java.com.lazymachine.core;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import main.java.com.lazymachine.math.Trigonometry;

// Single texture based, ray casted voxel engine inspired by "Black Art of 3D Game Programming" pp 935-989
// Author: Blake Hunter
// Date: 10/13/12
public class HeightMappedVoxelEngineDriver {

    /** time at last frame */
    long lastFrame = 0;

    /** frames per second */
    int fps = 0;

    /** last fps time */
    long lastFPS = 0;

    private static final int DEGREES_IN_CIRCLE = 360;
    private static final int DEFAULT_X_SCREEN_RESOLUTION = 1024;
    private static final int DEFAULT_Y_SCREEN_RESOLUTION = 1024;
    private final int X_SCREEN_RESOLUTION;
    private final int Y_SCREEN_RESOLUTION;
    private final int SCREEN_X_AXIS_MAX;
    private final int SCREEN_X_AXIS_MIN;
    private final int SCREEN_Y_AXIS_MAX;
    private final int SCREEN_Y_AXIS_MIN;
    private static final int DEFAULT_MAP_X_LENGTH = 1024;
    private static final int DEFAULT_MAP_Y_LENGTH = 1024;  
    private final int MAP_X_LENGTH; // WORLD_X_SIZE
    private final int MAP_Y_LENGTH; // WORLD_Y_SIZE
    private final int FIELD_OF_VIEW;
    private final double ANGULAR_INCREMENT;

    // Constants used to represent angles for the ray casting a FIELD_OF_VIEW degree field of view
    private final int ANGLE_0;
    private final int ANGLE_1;
    private final int ANGLE_2;
    private final int ANGLE_4;
    private final int ANGLE_5;
    private final int ANGLE_6;
    private final int ANGLE_15;
    private final int ANGLE_30;
    private final int ANGLE_45;
    private final int ANGLE_60;
    private final int ANGLE_90;
    private final int ANGLE_135;
    private final int ANGLE_180;
    private final int ANGLE_225;
    private final int ANGLE_270;
    private final int ANGLE_315;
    private final int ANGLE_360;
    private Texture mapTexture; //image_pcx in the book
    private final String VERTICAL_LINE_TEST_PATH_AND_FILENAME = "src/resources/new-vertical-line-test.png";
    private final int BITS_PER_PIXEL = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha for PNG image

    private int playerCurrentXCoordinate; // play_x in the book
    private int playerCurrentYCoordinate; // play_y
    private int playerCurrentZCoordinate; // play_z (height)
    private int playerCurrentViewingAngle; // play_ang
    private int playerDistanceFromViewPlane; // play_dist
    private int voxelScalingFactor; // mountain_scale

    private double playerXDirection; // play_dir_x // the direction the player is going in
    private double playerYDirection; // play_dir_y
    private double playerZDirection; // play_dir_z
    private double[] sphericalDistortionCancellationLookup; // sphere_cancel
    private boolean debug;
    private boolean hasScreenshotBeenTaken;
    private byte[] textureData;
    
    private LookupTableProvider lookupTableProvider;
    private VerticalLineRenderer verticalLineRenderer;

    public HeightMappedVoxelEngineDriver(int fieldOfVision) {
        this(fieldOfVision, false);
    }

    public HeightMappedVoxelEngineDriver(int fieldOfVision, boolean debug) {
        this(DEFAULT_X_SCREEN_RESOLUTION, DEFAULT_Y_SCREEN_RESOLUTION, fieldOfVision, DEFAULT_MAP_X_LENGTH, DEFAULT_MAP_Y_LENGTH);
        this.debug = debug;
    }

    public HeightMappedVoxelEngineDriver(int xScreenResolution, int yScreenResolution, int fieldOfVision, int universeXLength, int universeYLength) {
        this.playerCurrentXCoordinate = 30000; // 30000 works well but I will need to boundry code so 3000 works (it looks better)
        this.playerCurrentYCoordinate = 30000; // 
        this.playerCurrentZCoordinate = 600; // default 300 (600 looks awesome)
        X_SCREEN_RESOLUTION  = xScreenResolution;
        Y_SCREEN_RESOLUTION = yScreenResolution;
        this.SCREEN_X_AXIS_MIN = 0;
        this.SCREEN_Y_AXIS_MIN = 0;
        this.SCREEN_X_AXIS_MAX = xScreenResolution;
        this.SCREEN_Y_AXIS_MAX = yScreenResolution;
        MAP_X_LENGTH = universeXLength;
        MAP_Y_LENGTH = universeYLength;
        
        // TODO: remove this after I find a way to start the player at x, y coordinates (304, 304) instead of 30000
        System.out.println("this.playerCurrentXCoordinate % MAP_X_LENGTH: " + this.playerCurrentXCoordinate % MAP_X_LENGTH);
        System.out.println("this.playerCurrentYCoordinate % MAP_X_LENGTH: " + this.playerCurrentYCoordinate % MAP_Y_LENGTH);
        
        FIELD_OF_VIEW = fieldOfVision;
        // TODO: A better approach might be to create a HashMap to just lookup the calculated angle
        ANGLE_0 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION , 0);
        ANGLE_1 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION , 1);
        ANGLE_2 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION , 2);
        ANGLE_4 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION , 4);
        ANGLE_5 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION , 5);
        ANGLE_6 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION , 6);
        ANGLE_15 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION , 15);
        ANGLE_30 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 30);
        ANGLE_45 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 45);
        ANGLE_60 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 60);
        ANGLE_90 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 90);
        ANGLE_135 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 135);
        ANGLE_180 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 180);
        ANGLE_225 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 225);
        ANGLE_270 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 270);
        ANGLE_315 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 315);
        ANGLE_360 = calculateAnglesForRayCastingFieldOfView(FIELD_OF_VIEW, X_SCREEN_RESOLUTION, 360);
        ANGULAR_INCREMENT = ((double) DEGREES_IN_CIRCLE) / ANGLE_360;
        this.sphericalDistortionCancellationLookup = new double[ANGLE_60]; // This is for a 60 degree FOV (need to adjust for other FOVs)
        this.hasScreenshotBeenTaken = false;
        playerCurrentViewingAngle = ANGLE_90;
        playerDistanceFromViewPlane = 40; // default 70 (40 looks good)
        voxelScalingFactor = 60; // default 10  (60 looks good)      
    }

    public void drawTerrain(int playerCurrentXCoordinate, int playerCurrentYCoordinate, int playerCurrentZCoordinate, 
            int playerCurrentViewingAngle, int playerDistanceFromViewPlane) {
        // this function draws the entire terrain based on the location and orientation of the player's viewpoint

        // Make sure the player's coordinates stay within the world boundries
        // TODO: get this working
//        playerCurrentXCoordinate = playerCurrentXCoordinate % MAP_X_LENGTH; // Use %= operator
//        playerCurrentYCoordinate = playerCurrentYCoordinate % MAP_Y_LENGTH; // Use %= operator
        
        int rayXCoordinate = 0; // xr : location of ray in world x coordinate
        int rayYCoordinate = 0; // yr : location of ray in world y coordinate
        int rayHitXTextureCoordinate = 0; // x_fine : the x texture coordinate the ray hit
        int rayHitYTextureCoordinate = 0; // y_fine : the y texture coordinate the ray hit
        int pixelColor = 0; // pixel_color : the color of textel
        int currentRay = 0; // ray : looping variable
        int currentRow = 0; // row : the current video row being processed
        int currentStripScale = 0; // scale : the scale of the current strip
        int topOfStrip = 0; // top : top of strip
        int bottomOfStrip = 0; // bottom : bottom of strip
        double rayLength = 0; // ray_length : the length of the ray after distortion compensation
        int maxRows = 1024;

        // curr_ang : current angle being processed
        // start the current angle off -30 degrees to the left of the player's current viewing direction
        int currentAngle = playerCurrentViewingAngle - ANGLE_30;

        // check for underflow
        currentAngle = checkForAngleUnderflow(currentAngle);


        // cast a series of rays for every column of the screen (why does this start at 1 in the book? A: because with 0 the raylegnth is NaN) 
        for (currentRay = 1; currentRay < (SCREEN_X_AXIS_MAX); currentRay++) { 
            if (this.playerCurrentZCoordinate < 1024) {
                maxRows = this.playerCurrentZCoordinate;
            } else {
                maxRows = 1024;
            }

            // Using the painters algorithm (drawing back to front) we start at the top (the furthest objects) and move down
            // drawing to the front (the closest objects)
            // Note: if currentRow == 0 then it is never processed so we don't get Nan for the rayLength
            for (currentRow = maxRows-1; currentRow > 0; currentRow--) {
                rayLength = sphericalDistortionCancellationLookup[currentRay] * ((double)playerDistanceFromViewPlane * playerCurrentZCoordinate) / ((double)playerCurrentZCoordinate - currentRow);

                // Cut the draw distance so we aren't drawing way out into the background (TODO: fix magic number)
                // If the ray length is greater than 1024 then don't draw it and move on to the next ray (TODO: find a way to optimize this)
                if (rayLength < 480) {
                    double[] cosineLookup = lookupTableProvider.cosineLookup();
                    double[] sineLookup = lookupTableProvider.sineLookup();
                    rayXCoordinate = (int) ((double)playerCurrentXCoordinate + rayLength * cosineLookup[currentAngle]); 
                    rayYCoordinate = (int) ((double)playerCurrentYCoordinate - rayLength * sineLookup[currentAngle]);

                    // compute texture coordinates
//                    rayHitXTextureCoordinate = rayXCoordinate % MAP_X_LENGTH;
//                    rayHitYTextureCoordinate = rayYCoordinate % MAP_Y_LENGTH;
                    
                    // optimized with bitwise operator (MAP_X_LENGTH-1) == 1023 == Ox03FF
                    rayHitXTextureCoordinate = rayXCoordinate & 0x03FF ;
                    rayHitYTextureCoordinate = rayYCoordinate & 0x03FF;
                    
                    // Using texture index locate texture pixel in textures (pixelColor here is height) TODO: rename to voxelHieght and use color map to get real pixelColor variable
//                    pixelColor = textureData[rayHitXTextureCoordinate + (rayHitYTextureCoordinate * SCREEN_X_AXIS_MAX)] & 0xFF;
                    
                    // Optimized with lookup table
                    int[] rayHitYTextureCoordinateLookup = lookupTableProvider.rayHitYTextureCoordinateLookupTable();
                    pixelColor = textureData[rayHitXTextureCoordinate + (rayHitYTextureCoordinateLookup[rayHitYTextureCoordinate])] & 0xFF;
                    
                    currentStripScale = ((int)(voxelScalingFactor) * pixelColor) / ((int)rayLength + 1); // why + 1?
                    
                    bottomOfStrip = currentRow;
                    topOfStrip = bottomOfStrip + currentStripScale;

                    verticalLineRenderer.drawVerticalLineSegment(topOfStrip, bottomOfStrip, currentRay, pixelColor);
                }
            }
            // move to next angle
            if (++currentAngle >= ANGLE_360) {
                currentAngle = ANGLE_0;
            }
        }
    }

    private int checkForAngleUnderflow(int currentAngle) {
        if (currentAngle < 0) {
            currentAngle += ANGLE_360;
        }
        return currentAngle;
    }

    // this code builds all the look up tables for the terrain generator and loads in the terrain texture map
    // TODO: write unit tests for this if possible or refactor to make it possible.
    public void initializeVoxelEngine() {
        try {
            createInverseCosineViewingDistortionFilter();
            lookupTableProvider = new LookupTableProvider(MAP_X_LENGTH, SCREEN_X_AXIS_MAX, 
                                                          ANGLE_360, ANGULAR_INCREMENT);
            verticalLineRenderer = new VerticalLineRenderer(MAP_X_LENGTH, SCREEN_X_AXIS_MAX, 
                                                            ANGLE_360, ANGULAR_INCREMENT);
            loadGameMap();         
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createInverseCosineViewingDistortionFilter() {
        double currentRadianAngle;
        int angle;
        for (angle = 0; angle < ANGLE_30; angle++) {
            currentRadianAngle = Trigonometry.computeAngleInRadians(angle, ANGULAR_INCREMENT);
            sphericalDistortionCancellationLookup[angle + ANGLE_30] = 1 / Math.cos(currentRadianAngle);
            sphericalDistortionCancellationLookup[ANGLE_30 - angle] = 1 / Math.cos(currentRadianAngle);
        }
    }

    private void loadGameMap() throws IOException {
      System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
      
        this.mapTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("src/main/resources/commanche-D1.png"));
        GL11.glDisable(GL11.GL_TEXTURE_2D); // This fixes the issue above causing the screen to be darker
        textureData = mapTexture.getTextureData();
    }

    public void start() {
        try {
            Display.setDisplayMode(new DisplayMode(X_SCREEN_RESOLUTION, Y_SCREEN_RESOLUTION));
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        initGL(); // init OpenGL
        initializeVoxelEngine();
        getDelta(); // call once before loop to initialize lastFrame
        lastFPS = getTime(); // call before loop to initialize fps timer

        while (!Display.isCloseRequested()) {
            int delta = getDelta();

            update(delta);
            renderGL();

            Display.update();
            Display.sync(60); // cap fps to 60fps
        }            
        Display.destroy();
    }

    public void update(int delta) {
        // change viewing distance further
        if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
            playerDistanceFromViewPlane += 1;
        }

        // change viewing distance closer
        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            playerDistanceFromViewPlane -= 1;
        }

        // Move forward in the voxel world
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            int speed = 10;

            // compute trajectory for this viewing angle
            double[] cosineLookup = lookupTableProvider.cosineLookup();
            double[] sineLookup = lookupTableProvider.sineLookup();
            playerXDirection = cosineLookup[playerCurrentViewingAngle];
            playerYDirection = -sineLookup[playerCurrentViewingAngle];
            playerZDirection = 0;

            // translate viewpoint
            playerCurrentXCoordinate += speed * playerXDirection;
            playerCurrentYCoordinate += speed * playerYDirection;
            playerCurrentZCoordinate += speed * playerZDirection;
        }

        // Move backward in the voxel world
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            int speed = 10;

            // compute trajectory for this viewing angle
            double[] cosineLookup = lookupTableProvider.cosineLookup();
            double[] sineLookup = lookupTableProvider.sineLookup();
            playerXDirection = cosineLookup[playerCurrentViewingAngle];
            playerYDirection = -sineLookup[playerCurrentViewingAngle];
            playerZDirection = 0;

            // translate viewpoint
            playerCurrentXCoordinate -= speed * playerXDirection;
            playerCurrentYCoordinate -= speed * playerYDirection;
            playerCurrentZCoordinate -= speed * playerZDirection;
        }

        // Change viewing height up in the voxel world
        if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
            playerCurrentZCoordinate += 1;
        }

        // Change viewing height down in the voxel world
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            playerCurrentZCoordinate -= 1;
        }

        // rotate right in the voxel world
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            if ((playerCurrentViewingAngle += ANGLE_5) >= ANGLE_360) {
                playerCurrentViewingAngle -= ANGLE_360;
            }
        }

        // rotate left in the voxel world
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            if ((playerCurrentViewingAngle -= ANGLE_5) < 0) {
                playerCurrentViewingAngle += ANGLE_360;
            }
        }

        updateFPS(); // update FPS Counter
    }

    /** 
     * Calculate how many milliseconds have passed 
     * since last frame.
     * 
     * @return milliseconds passed since last frame 
     */
    public int getDelta() {
        long time = getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;

        return delta;
    }

    /**
     * Get the accurate system time
     * 
     * @return The system time in milliseconds
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    /**
     * Calculate the FPS and set it in the title bar
     */
    public void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    public void initGL() {
        GL11.glOrtho(0, X_SCREEN_RESOLUTION, 0, Y_SCREEN_RESOLUTION, 1, -1);
    }

    public void renderGL() {
        // Clear The Screen And The Depth Buffer
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        drawTerrain(playerCurrentXCoordinate, playerCurrentYCoordinate, playerCurrentZCoordinate, playerCurrentViewingAngle, playerDistanceFromViewPlane);
    }

    int calculateAnglesForRayCastingFieldOfView(int fieldOfViewDegrees, int numberOfColumns, int angle) {
        int formattedAngle = getFixedRangeOfDegrees(angle);
        int angularIncrementMultiplier = DEGREES_IN_CIRCLE / fieldOfViewDegrees;
        int numberOfAngularIncrements = numberOfColumns * angularIncrementMultiplier;

        return (numberOfAngularIncrements * formattedAngle) / DEGREES_IN_CIRCLE;
    }

    int getFixedRangeOfDegrees(int angle) {
        int fixedAngle = angle;
        if (angle < 0) {
            fixedAngle = DEGREES_IN_CIRCLE - (Math.abs(angle) % DEGREES_IN_CIRCLE);
        } else if (angle == 360) {
            return 360;
        }
        return fixedAngle % DEGREES_IN_CIRCLE;
    }

    private void drawVerticalLineSegmentsForEntireScreen() {
        int black = 0;
        int white = 255;
        int currentColor = white;

        for (int currentXCoordinate = 0; currentXCoordinate < SCREEN_X_AXIS_MAX; currentXCoordinate++) {
            verticalLineRenderer.drawVerticalLineSegment(SCREEN_Y_AXIS_MAX, SCREEN_Y_AXIS_MIN, currentXCoordinate, currentColor);
            if (currentColor == white) {
                currentColor = black;
            } else {
                currentColor = white;
            }
        }
    }

    public static void main(String[] argv) {
        HeightMappedVoxelEngineDriver HeightMappedVoxelEngineDriver = new HeightMappedVoxelEngineDriver(60);
        HeightMappedVoxelEngineDriver.start();
    }
    
}
