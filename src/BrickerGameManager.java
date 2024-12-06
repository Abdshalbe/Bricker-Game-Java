import danogl.gui.rendering.TextRenderable;
import gameobjects.*;
import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.*;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Counter;
import danogl.util.Vector2;


import java.awt.*;
import java.util.Random;

public class BrickerGameManager extends GameManager {

    private static final float BALL_SPEED = 250;
    private static final int BALL_RADIUS = 35;
    private static final int PADLLE_LENGTH = 100;
    private static final int PADLLE_WIDTH = 15;
    private static final int SCREEN_LEGTH = 700 ;
    private static final int SCREEN_WIDTH = 500;
    private static final float RESET_BALL_POSITION = 0.5f;
    private static final float BALL_DIMINISIONS = 20;
    private static final int INITIAL_VALOSITY = 200;
    private static final int TARGET_SIZE = 80 ;
    private static final int PADDLE_LENGTH = 100;
    private static final int PADDLE_WIDTH = 15;
    private static final int MARGIN_VAL = 15;
    private Ball ball;
    private Vector2 windowDimensions;
    private WindowController windowController;
    private static final int MAX_LIVES = 4;
    private int remainingLives;
    private GameObject[] hearts = new GameObject[MAX_LIVES];
    private static final int HEART_SIZE = 40;
    private Counter remainingBricks;
    private GameObject numericLivesText;
    private Counter livesCounter;
    private NumericLifeCounter numericLifeCounter;  // Declare the NumericLifeCounter object



    public BrickerGameManager(String windowTitle, Vector2 windowDimensions){
        super(windowTitle,windowDimensions);
    }
    /**
     * constructor to build the game
     * */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader, UserInputListener inputListener, WindowController windowController) {

        this.windowController = windowController;
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        windowController.setTargetFramerate(TARGET_SIZE);
        windowDimensions = windowController.getWindowDimensions();
        remainingLives = MAX_LIVES;


        Vector2 windowDimensions = windowController.getWindowDimensions();// return 700,500
        // creating ball
        createBall(imageReader, soundReader, windowController);
        //create user paddle
        createUserPaddle(imageReader, inputListener, windowController);
        //creating walls
        final int BORDER_WIDTH=10;
        Color BORDER_COLOR = Color.GRAY;
        // create left wall
        createWall(Vector2.ZERO,new Vector2(BORDER_WIDTH, windowDimensions.y()),BORDER_COLOR);
        //create right wall
        createWall(new Vector2(windowDimensions.x()-BORDER_WIDTH , 0),new Vector2(BORDER_WIDTH, windowDimensions.y()),BORDER_COLOR);
        //create up wall
        createWall(Vector2.ZERO,new Vector2(windowDimensions.x(), BORDER_WIDTH), BORDER_COLOR);

        //create Background
        Renderable backgroundImage = imageReader.readImage("assets/DARK_BG2_small.jpeg",false);
        GameObject background = new GameObject(Vector2.ZERO,windowController.getWindowDimensions(), backgroundImage);
        background.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        //Uniting Background with the game
        gameObjects().addGameObject(background, Layer.BACKGROUND);


        // create brick
        createBrickes(imageReader);
//        gameObjects().addGameObject(largeBrick, Layer.STATIC_OBJECTS);

        // create hearts
        livesCounter = new Counter(MAX_LIVES); // Ensure this is initialized before calling the method
//        createNumericLifeCounter();
        // Initialize with the maximum lives
        windowDimensions = windowController.getWindowDimensions();
        createHearts(imageReader);

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        checkForGameEnd();
    }
    private void createHearts(ImageReader imageReader, UserInputListener inputListener, WindowController windowController) {
        Renderable heartImage = imageReader.readImage("assets/heart.png", true);

        for (int i = 0; i < MAX_LIVES; i++) {
            Vector2 position = new Vector2(30+10 + i * (HEART_SIZE + 5), windowDimensions.y() - HEART_SIZE -10);

            hearts[i] = new GraphicLifeCounter(
                    position,
                    new Vector2(HEART_SIZE, HEART_SIZE),
                    new Counter(MAX_LIVES - i), // Individual Counter
                    heartImage,
                    gameObjects(),
                    i + 1 // Each heart represents a life index
            );

            gameObjects().addGameObject(hearts[i], Layer.UI);
        }



    // Render numeric life count
        Renderable textRenderable = new TextRenderable(String.valueOf(remainingLives));
        numericLivesText = new GameObject(
                new Vector2(10, windowDimensions.y() - HEART_SIZE - 60), // Position below hearts
                new Vector2(50, 30), // Text box dimensions
                textRenderable
        );

        gameObjects().addGameObject(numericLivesText, Layer.UI);
    }

    private void checkForGameEnd() {
        float ballHeight = ball.getCenter().y();
        if (ball == null) return;

        String prompt = "";
        if (remainingBricks.value() == 0) { // All bricks destroyed
            prompt = "You Win! All bricks destroyed!";
        } else if (ballHeight > windowDimensions.y()) { // Ball fell below screen
            if (remainingLives > 1) {
                remainingLives--;
                livesCounter.decrement();
                // Remove the heart corresponding to the lost life
                gameObjects().removeGameObject(hearts[remainingLives], Layer.UI);

                // Update the numeric life counter automatically, no need to update manually
                resetBall();  // Reset ball position and velocity
            } else {
                prompt = "You Lose! Play again?";
            }
        }

        if (!prompt.isEmpty()) {
            if (windowController.openYesNoDialog(prompt)) {
                windowController.resetGame();  // Reset the game
            } else {
                windowController.closeWindow();  // Close the window if game over
            }
        }
    }

    private void createHearts(ImageReader imageReader) {
        Renderable heartImage = imageReader.readImage("assets/heart.png", true);

        for (int i = 0; i <MAX_LIVES; i++) {
            Vector2 position = new Vector2(50 + i * (HEART_SIZE + 5), windowDimensions.y() - HEART_SIZE -10);

            hearts[i] = new GraphicLifeCounter(
                    position,
                    new Vector2(HEART_SIZE, HEART_SIZE),
                    new Counter(MAX_LIVES ), // Individual Counter
                    heartImage,
                    gameObjects(),
                    i + 1
            );

            gameObjects().addGameObject(hearts[i], Layer.UI);
        }

        // Render numeric life count below hearts
        Renderable textRenderable = new TextRenderable(String.valueOf(remainingLives));
        numericLivesText = new NumericLifeCounter(livesCounter,
                new Vector2(20,SCREEN_WIDTH-50), // Position below hearts
                new Vector2(100, 35), // Text box dimensions
                gameObjects()
        );

        gameObjects().addGameObject(numericLivesText, Layer.UI);
    }
    private void resetBall() {
        ball.setCenter(windowDimensions.mult(RESET_BALL_POSITION));
        ball.setVelocity(Vector2.DOWN.mult(BALL_SPEED));
    }
    private void createUserPaddle(ImageReader imageReader, UserInputListener inputListener, WindowController windowController) {
        Renderable paddleImage = imageReader.readImage("assets/paddle.png", true);
        Vector2 windowDimensions = windowController.getWindowDimensions();

        GameObject userPaddle = new Paddle(Vector2.ZERO, new Vector2(PADDLE_LENGTH, PADDLE_WIDTH),paddleImage,
                inputListener, windowDimensions);

        userPaddle.setCenter(new Vector2(windowDimensions.x() / 2, windowDimensions.y() - 20));
        gameObjects().addGameObject(userPaddle);
    }
    private void createBall(ImageReader imageReader, SoundReader soundReader, WindowController windowController) {
        Renderable ballImage = imageReader.readImage("assets/ball.png", true);
        Sound collisionSound = soundReader.readSound("assets/blop.wav");
        ball = new Ball(Vector2.ZERO, new Vector2(BALL_RADIUS, BALL_RADIUS), ballImage, collisionSound);

        Vector2 windowDimensions = windowController.getWindowDimensions();
        ball.setCenter(windowDimensions.mult(0.5f)); // Center the ball in the window

        // Set random initial velocity
        float ballVelX = BALL_SPEED;
        float ballVelY = BALL_SPEED;
        Random rand = new Random();
        if (rand.nextBoolean()) ballVelX *= -1;
        if (rand.nextBoolean()) ballVelY *= -1;

        ball.setVelocity(new Vector2(ballVelX, ballVelY));
        gameObjects().addGameObject(ball);
    }
    private void createGraphicLifeCounter(ImageReader imageReader) {
        Renderable heartImage = imageReader.readImage("assets/heart.png", true);
        for (int i = 0; i < MAX_LIVES; i++) {
            int numOfLives = i + 1;
            GameObject graphicLifeCounter = new GraphicLifeCounter(Vector2.ZERO, new Vector2(HEART_SIZE,
                    HEART_SIZE), livesCounter, heartImage, gameObjects(), numOfLives);
            graphicLifeCounter.setCenter(new Vector2(16 + HEART_SIZE * i, windowDimensions.y() - 16));
            gameObjects().addGameObject(graphicLifeCounter, Layer.UI);
        }
    }    private void createWall(Vector2 position, Vector2 dimensions, Color color){

        GameObject wall = new GameObject(position,dimensions,new RectangleRenderable(color));
        gameObjects().addGameObject(wall);

    }

    private void createBrickes(ImageReader imageReader){
        /**
         * creats the intial bricks number
         * */
        Renderable bricksImage = imageReader.readImage("assets/brick.png", true);
        GameObjectCollection gameObjects = gameObjects();
        BasicCollisionStrategy basicCollisionStrategy = new BasicCollisionStrategy(gameObjects , remainingBricks);

        remainingBricks = new Counter(); // Initialize the Counter for remaining bricks

        //create a lot of bricks
        int numRows = 2;
        int numCols = 2;
        float brickHeight = 15;
        float margin = 5;

        float brickWidth = (windowDimensions.x() - 2 *margin - (numCols -1)) / numCols;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                float x =  col * (brickWidth + 1);
                float y =  row * (brickHeight + 1);
                Renderable brickImage = imageReader.readImage("assets/brick.png", true);
                GameObject brick = new Brick(
                        new Vector2(x, y+MARGIN_VAL),
                        new Vector2(brickWidth, brickHeight),
                        brickImage,
                        new BasicCollisionStrategy(gameObjects(), remainingBricks)
                );
                gameObjects().addGameObject(brick ,Layer.STATIC_OBJECTS);
                remainingBricks.increment();// Increment the Counter for each brick created
            }
        }

    }


    public static void main(String[] args) {
        new BrickerGameManager("Bouncing ball", new Vector2(SCREEN_LEGTH,SCREEN_WIDTH)).run();
    }
}
