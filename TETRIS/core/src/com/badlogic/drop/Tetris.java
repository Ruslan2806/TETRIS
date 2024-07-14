package com.badlogic.drop;

import static com.badlogic.gdx.math.MathUtils.random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Iterator;

public class Tetris extends ApplicationAdapter {
    private Texture[] tileImage;
    private Texture currentTileImage;
    private Texture fieldImage;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Array<Rectangle> tile, b;
    private int[][] figures = {
            {1, 3, 5, 7},
            {2, 4, 5, 7},
            {3, 5, 4, 6},
            {3, 5, 4, 7},
            {2, 3, 5, 7},
            {3, 5, 7, 6},
            {2, 3, 4, 5}
    };
    private float lastMoveTime = 0;
    private float lastFallTime = 0;
    private float moveDelay = 0.1f; // задержка в секундах между перемещениями плитки
    private float moveDelayForFall = 0.5f; // задержка в секундах между паданием плитки
    public static final int BOARD_WIDTH = 15; // ширина игрового поля в плитках
    public static final int BOARD_HEIGHT = 25; // высота игрового поля в плитках
    private int checkfield[][];
    public static final int TILE_SIZE = 34; // размер одной плитки в пикселях
    private int indCurrentTile = 5; // индекс текущей плитки (соответствует форме)
    private boolean wasUpPressed = false;
    private String[] style1 = {"blue.png", "green.png", "orange.png", "red.png", "violet.png"};
    private String[] style2 = {"blue1.png", "green1.png", "orange1.png", "red1.png", "violet1.png"};
    private int numberOfColor;

    @Override
    public void create() {
        numberOfColor = random.nextInt(5);
        tileImage = new Texture[style1.length];
        for (int i = 0; i < style1.length; i++)
            tileImage[i] = new Texture(Gdx.files.internal(style2[i]));
        currentTileImage = tileImage[numberOfColor];
        fieldImage = new Texture(Gdx.files.internal("field1.png"));
        camera = new OrthographicCamera();
        viewport = new FitViewport(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE, camera);
        camera.setToOrtho(false, BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE);
        batch = new SpriteBatch();

        tile = new Array<Rectangle>(4);
        b = new Array<Rectangle>(4);
        checkfield = new int[BOARD_WIDTH][BOARD_HEIGHT];
        for (int i = 0; i < BOARD_WIDTH; i++)
            for (int j = 0; j < BOARD_HEIGHT; j++)
                checkfield[i][j] = -1;

        createNewTile();
    }

    public static String getRandomElement(String[] array) {
        int index = random.nextInt(array.length);
        return array[index];
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0.1f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        for (int i = 0; i < BOARD_WIDTH; i++)
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                if (checkfield[i][j] == -1) batch.draw(fieldImage, i * TILE_SIZE, j * TILE_SIZE);
                else batch.draw(tileImage[checkfield[i][j]], i * TILE_SIZE, j * TILE_SIZE);
            }

        for (Rectangle r : tile) {
            batch.draw(currentTileImage, r.x * TILE_SIZE, r.y * TILE_SIZE);
        }
        batch.end();

        removeFullRows();
        rotateTile();
        fallTile();
        handleInput();
    }

    private void removeFullRows() {
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean fullRow = true;

            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (checkfield[x][y] == -1) {
                    fullRow = false;
                    break;
                }
            }

            if (fullRow) {
                for (int yy = y; yy < BOARD_HEIGHT - 1; yy++) {
                    for (int x = 0; x < BOARD_WIDTH; x++) {
                        checkfield[x][yy] = checkfield[x][yy + 1];
                    }
                }

                for (int x = 0; x < BOARD_WIDTH; x++) {
                    checkfield[x][BOARD_HEIGHT - 1] = -1;
                }

                y--;
            }
        }
    }

    private boolean check() {
        for (int i = 0; i < 4; i++) {
            if (tile.get(i).y < 0 || tile.get(i).x < 0 || tile.get(i).x >= BOARD_WIDTH || tile.get(i).y >= BOARD_HEIGHT)
                return false;
            if (checkfield[(int) tile.get(i).x][(int) tile.get(i).y] != -1)
                return false;
        }
        return true;
    }

    private void fallTile() {
        float currentTime = TimeUtils.nanoTime() / 1_000_000_000.0f;
        float currentMoveDelay = moveDelayForFall;

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            currentMoveDelay = moveDelayForFall / 20;
        }

        if (currentTime - lastFallTime >= currentMoveDelay) {
            for (Rectangle r : tile) {
                r.y -= 1;
            }
            if (!check()) {
                for (Rectangle r : tile) {
                    r.y += 1;
                    checkfield[(int) r.x][(int) r.y] = numberOfColor;
                }
                createNewTile();
            }
            lastFallTime = currentTime;
        }

    }


    private void createNewTile() {
        tile.clear();
        numberOfColor = random.nextInt(5);
        currentTileImage = tileImage[numberOfColor];
        indCurrentTile = random(figures.length - 1);
        for (int i = 0; i < 4; i++) {
            tile.add(new Rectangle(
                    BOARD_WIDTH / 2 + figures[indCurrentTile][i] % 2,
                    BOARD_HEIGHT - figures[indCurrentTile][i] / 2,
                    TILE_SIZE,
                    TILE_SIZE));
        }
    }

    private void rotateTile() {
        boolean isUpPressed = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        if ((isUpPressed) && !wasUpPressed) {
            Array<Rectangle> rotatedTile = new Array<>(4);
            Rectangle p = tile.get(1);
            for (int i = 0; i < 4; i++) {
                int x = (int) (tile.get(i).y - p.y);
                int y = (int) (tile.get(i).x - p.x);
                rotatedTile.add(new Rectangle(p.x - x, p.y + y, TILE_SIZE, TILE_SIZE));
            }
            if (checkRotation(rotatedTile)) {
                tile = rotatedTile;
            }
        }
        wasUpPressed = isUpPressed;
    }

    private boolean checkRotation(Array<Rectangle> rotatedTile) {
        for (Rectangle r : rotatedTile) {
            if (r.y < 0 || r.x < 0 || r.x >= BOARD_WIDTH || r.y >= BOARD_HEIGHT) {
                return false;
            }
            if (checkfield[(int) r.x][(int) r.y] != -1) {
                return false;
            }
        }
        return true;
    }

    private void handleInput() {
        float currentTime = TimeUtils.nanoTime() / 1_000_000_000.0f; // текущее время в секундах

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (currentTime - lastMoveTime >= moveDelay) {
                moveTiles(1);
                lastMoveTime = currentTime;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (currentTime - lastMoveTime >= moveDelay) {
                moveTiles(-1);
                lastMoveTime = currentTime;
            }
        }
    }

    private void moveTiles(int direction) {
        boolean canMove = true;
        for (Rectangle r : tile) {
            if (r.x + direction < 0 || r.x + direction >= BOARD_WIDTH) {
                canMove = false;
                break;
            }
        }
        if (canMove) {
            for (Rectangle r : tile) {
                r.x += direction;
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (Texture t : tileImage)
            t.dispose();
    }
}
