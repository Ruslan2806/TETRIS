package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Tetris extends ApplicationAdapter {
    private Texture tileImage;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Array<Rectangle> tile;
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
    private static final int BOARD_WIDTH = 10; // ширина игрового поля в плитках
    private static final int BOARD_HEIGHT = 20; // высота игрового поля в плитках
    private static final int TILE_SIZE = 34; // размер одной плитки в пикселях
    private int indCurrentTile = 3; // индекс текущей плитки (соответствует форме)

    @Override
    public void create() {
        tileImage = new Texture(Gdx.files.internal("red1.png"));
        camera = new OrthographicCamera();
        viewport = new FitViewport(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE, camera);
        camera.setToOrtho(false, BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE);
        batch = new SpriteBatch();

        tile = new Array<Rectangle>(4);
        //    int n = 3;
        for (int i = 0; i < 4; i++) {
            tile.add(new Rectangle(BOARD_WIDTH / 2 - figures[indCurrentTile][i] % 2, BOARD_HEIGHT + figures[indCurrentTile][i] / 2, TILE_SIZE, TILE_SIZE));
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0.1f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        for (Rectangle r : tile) {
            batch.draw(tileImage, r.x * TILE_SIZE, r.y * TILE_SIZE);
        }
        batch.end();

        rotateTile();
        fallTile();
        handleInput();
    }

    private void fallTile() {
        float currentTime = TimeUtils.nanoTime() / 1_000_000_000.0f;
        if (currentTime - lastFallTime >= moveDelayForFall) {
            for (Rectangle r : tile) {
                r.y -= 1;
                lastFallTime = currentTime;
            }
        }
    }

    private boolean wasUpPressed = false;
    private void rotateTile() {
        boolean isUpPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        if (isUpPressed && !wasUpPressed) {
            Rectangle p = tile.get(indCurrentTile);
            for (int i = 0; i < 4; i++) {
                int x = (int) (tile.get(i).y - p.y);
                int y = (int) (tile.get(i).x - p.x);
                tile.get(i).x = p.x - x;
                tile.get(i).y = p.y + y;
            }
        }
        wasUpPressed = isUpPressed;
    }

    private void handleInput() {
        float currentTime = TimeUtils.nanoTime() / 1_000_000_000.0f; // текущее время в секундах

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (currentTime - lastMoveTime >= moveDelay) {
                moveTiles(1);
                lastMoveTime = currentTime;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (currentTime - lastMoveTime >= moveDelay) {
                moveTiles(-1);
                lastMoveTime = currentTime;
            }
        }
    }

    private void moveTiles(int direction) {
        boolean screenBorder = false;
        for (Rectangle r : tile) {
            if (r.x + direction < 0 || r.x + direction >= BOARD_WIDTH) {
                screenBorder = true;
                break;
            }
        }
        if (screenBorder == false) {
            for (Rectangle r : tile) r.x += direction;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        tileImage.dispose();
    }
}
