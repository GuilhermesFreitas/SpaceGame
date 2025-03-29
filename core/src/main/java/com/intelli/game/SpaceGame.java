package com.intelli.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

public class SpaceGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image, modularShips, bullets, shipsEnemy, explosionSprite, heart;
    private TextureRegion sprite, bullet, bulletEnemies, spriteEnemy, spriteBoss, explosions;
    private Sprite nave, projectile, projectileEnemies, enemyShip, bossShip, Explosion;
    private Music backgroundMusic;
    private Sound shootSound, explosionSound, gameOverExplosion;
    private float posX, posY, xProjectile, yProjectile, velocity, attackSpeed, bossProjectileSpeed;
    private boolean attack, explosionActive, gameOver, isBossActive;
    private long lastEnemyTime, explosionTime, lastBossShotTime;
    private Array<Rectangle> enemies, bossProjectiles;
    private float explosionX, explosionY;
    private int score, life, bossHealth, bossSpawnScore;
    private long enemySpawnInterval;
    private Rectangle boss, player;
    private boolean enemyAttack;
    private float xEnemyProjectile, yEnemyProjectile;
    private long lastEnemyShotTime;
    private float enemyAttackCooldown = 1.5f;
    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    private BitmapFont bitmap;
    private float bossDirectionX, bossDirectionY;
    private float bossSpeed;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("SpaceBg.png");
        modularShips = new Texture("modular_ships.png");
        shipsEnemy = new Texture("modular_ships.png");
        bullets = new Texture("BulletCollection.png");
        explosionSprite = new Texture("explosion.png");
        heart = new Texture("heart.png");

        sprite = new TextureRegion(modularShips, 80, 320, 32, 32);
        bullet = new TextureRegion(bullets, 362, 9, 13, 22);
        spriteEnemy = new TextureRegion(shipsEnemy, 200, 448, 24, 21);
        bulletEnemies = new TextureRegion(bullets, 379, 9, 13, 22);
        spriteBoss = new TextureRegion(modularShips, 207, 327, 49, 65);
        explosions = new TextureRegion(explosionSprite, 66, 2, 29, 29);

        nave = new Sprite(sprite);
        projectile = new Sprite(bullet);
        enemyShip = new Sprite(spriteEnemy);
        bossShip = new Sprite(spriteBoss);
        projectileEnemies = new Sprite(bulletEnemies);
        Explosion = new Sprite(explosions);

        player = new Rectangle(
                posX,
                posY,
                nave.getWidth(),
                nave.getHeight()
        );

        enemies = new Array<>();
        bossProjectiles = new Array<>();
        lastEnemyTime = 0;
        explosionActive = false;

        isBossActive = false;
        bossProjectileSpeed = 300;
        bossSpeed = 120;
        bossSpawnScore = 10;

        score = 0;
        life = 3;
        bossHealth = 15;

        enemySpawnInterval = 1_000_000_000;
        lastEnemyShotTime = 0;

        generator = new FreeTypeFontGenerator(Gdx.files.internal("Font/font.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        parameter.color = Color.WHITE;
        bitmap = generator.generateFont(parameter);

        posX = 608;
        posY = 10;
        xProjectile = nave.getWidth() / 2;
        yProjectile = nave.getHeight() / 2;

        enemyAttack = false;
        attack = false;

        velocity = 250;
        attackSpeed = 10;

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Soundtrack/inferior_diety_v2.ogg"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("Soundtrack/laser.wav"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("Soundtrack/explosion.wav"));
        gameOverExplosion = Gdx.audio.newSound(Gdx.files.internal("Soundtrack/gameOverExplosion.mp3"));


        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.3f);
        backgroundMusic.play();

        gameOver = false;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if (!gameOver) {
            moveShip();
            moveProjectile();
            updatePlayerRectangle();
            moveEnemies();
            moveEnemyProjectile();
            moveBoss();
        }

        batch.begin();
        batch.draw(image, 0, 0);

        for (int i = 0; i < life; i++) {
            batch.draw(heart, Gdx.graphics.getWidth() - (i + 1) * (heart.getWidth() + 10), Gdx.graphics.getHeight() - 50);
        }

        if (!gameOver) {
            if (attack) {
                batch.draw(projectile, xProjectile + nave.getWidth() / 2 - projectile.getWidth() / 2, yProjectile);
            }
            if (enemyAttack) {
                batch.draw(projectileEnemies, xEnemyProjectile, yEnemyProjectile);
            }
            batch.draw(nave, posX, posY);

            for (Rectangle enemy : enemies) {
                batch.draw(enemyShip, enemy.x, enemy.y);
            }

            if (isBossActive) {
                batch.draw(bossShip, boss.x, boss.y, boss.width, boss.height);
                for (Rectangle projectile : bossProjectiles) {
                    batch.draw(projectileEnemies, projectile.x, projectile.y);
                }
            }

            bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
            bitmap.draw(batch, "Level: " + (score / 10 + 1), 20, Gdx.graphics.getHeight() - 60);
        } else {
            bitmap.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 + 20);
            bitmap.draw(batch, "Final Score: " + score, Gdx.graphics.getWidth() / 2 - 70, Gdx.graphics.getHeight() / 2 - 20);
            bitmap.draw(batch, "Press ENTER to restart", Gdx.graphics.getWidth() / 2 - 120, Gdx.graphics.getHeight() / 2 - 60);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                resetGame();
            }
        }

        if (explosionActive && TimeUtils.timeSinceNanos(explosionTime) < 500_000_000) {
            batch.draw(Explosion, explosionX, explosionY);
        }

        batch.end();
    }

    private void resetGame() {
        score = 0;
        life = 3;
        posX = 608;
        posY = 10;
        enemies.clear();
        bossProjectiles.clear();
        gameOver = false;
        attack = false;
        enemyAttack = false;
        explosionActive = false;
        isBossActive = false;
        enemySpawnInterval = 1_000_000_000;
        backgroundMusic.play();
        player.x = posX;
        player.y = posY;
        player.width = nave.getWidth();
        player.height = nave.getHeight();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        modularShips.dispose();
        bullets.dispose();
        shipsEnemy.dispose();
        explosionSprite.dispose();
        backgroundMusic.dispose();
        shootSound.dispose();
        explosionSound.dispose();
        gameOverExplosion.dispose();
        generator.dispose();
        bitmap.dispose();
    }

    private void moveShip() {
        if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && posX < Gdx.graphics.getWidth() - nave.getWidth()) {
            posX += velocity * Gdx.graphics.getDeltaTime();
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && posX > 0) {
            posX -= velocity * Gdx.graphics.getDeltaTime();
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) && posY < Gdx.graphics.getHeight() - nave.getHeight()) {
            posY += velocity * Gdx.graphics.getDeltaTime();
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) && posY > 0) {
            posY -= velocity * Gdx.graphics.getDeltaTime();
        }

    }

    private void updatePlayerRectangle() {
        player.x = posX;
        player.y = posY;
    }

    private void moveProjectile() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !attack) {
            attack = true;
            xProjectile = posX + nave.getWidth() / 2 - projectile.getWidth() / 2;
            yProjectile = posY + nave.getHeight();
            shootSound.play(1.0f);
        }

        if (attack) {
            yProjectile += attackSpeed;

            if (isBossActive && collide(xProjectile, yProjectile, projectile.getWidth(), projectile.getHeight(),
                boss.x, boss.y, boss.width, boss.height)) {
                bossHealth--;
                attack = false;
                explosionSound.play(0.5f);
                explosionActive = true;
                explosionX = xProjectile;
                explosionY = yProjectile;
                explosionTime = TimeUtils.nanoTime();

                if (bossHealth <= 0) {
                    isBossActive = false;
                    score += 5;
                    explosionSound.play(0.8f);
                }
            }

            if (yProjectile > Gdx.graphics.getHeight()) {
                attack = false;
            }
        }
    }

    private void moveEnemyProjectile() {
        if (score >= 10 && !enemyAttack && TimeUtils.nanoTime() - lastEnemyShotTime > enemyAttackCooldown * 1_000_000_000L) {
            if (!enemies.isEmpty()) {
                Rectangle enemy = enemies.first();
                enemyAttack = true;
                xEnemyProjectile = enemy.x + enemy.width / 2 - projectile.getWidth() / 2;
                yEnemyProjectile = enemy.y;
                shootSound.play(0.3f);
                lastEnemyShotTime = TimeUtils.nanoTime();
            }
        }

        if (enemyAttack) {
            yEnemyProjectile -= attackSpeed;

            if (collide(xEnemyProjectile, yEnemyProjectile, projectile.getWidth(), projectile.getHeight(), player)) {
                explosionSound.play(0.3f);
                life--;
                enemyAttack = false;

                if (life <= 0) {
                    gameOverExplosion.play(0.5f);
                    backgroundMusic.stop();
                    gameOver = true;
                }
            }

            if (yEnemyProjectile + projectile.getHeight() < 0) {
                enemyAttack = false;
            }
        }
    }

    private void spawnEnemies() {
        Rectangle enemy = new Rectangle(
            MathUtils.random(0, Gdx.graphics.getWidth() - enemyShip.getWidth()),
            Gdx.graphics.getHeight(),
            enemyShip.getWidth(),
            enemyShip.getHeight()
        );
        enemies.add(enemy);
        lastEnemyTime = TimeUtils.nanoTime();
    }

    private void moveEnemies() {
        if (score >= bossSpawnScore && score % 10 == 0 && !isBossActive && enemies.size == 0) {
            spawnBoss();
            isBossActive = true;
            bossSpawnScore += 10;
        }

        if (TimeUtils.nanoTime() - lastEnemyTime > enemySpawnInterval) {
            spawnEnemies();
        }

        for (Iterator<Rectangle> iter = enemies.iterator(); iter.hasNext();) {
            Rectangle enemy = iter.next();
            enemy.y -= 400 * Gdx.graphics.getDeltaTime();

            if (attack && collide(enemy.x, enemy.y, enemy.width, enemy.height,
                xProjectile, yProjectile, projectile.getWidth(), projectile.getHeight())) {
                explosionSound.play(0.5f);
                explosionActive = true;
                explosionX = enemy.x;
                explosionY = enemy.y;
                explosionTime = TimeUtils.nanoTime();
                attack = false;
                iter.remove();
                score++;} else if (collide(enemy.x, enemy.y, enemy.width, enemy.height,
                posX, posY, nave.getWidth(), nave.getHeight())) {
                explosionSound.play(0.3f);
                life--;
                if (life <= 0) {
                    gameOverExplosion.play(0.5f);
                    backgroundMusic.stop();
                    gameOver = true;
                }
                iter.remove();
            }

            if (enemy.y + enemy.height < 0) {
                iter.remove();
            }
        }
    }

    private void moveBoss() {
        if (!isBossActive) return;

        if (bossDirectionX == 0 && bossDirectionY == 0) {
            bossDirectionX = MathUtils.randomSign();
            bossDirectionY = MathUtils.randomSign();
        }

        boss.x += bossDirectionX * bossSpeed * Gdx.graphics.getDeltaTime();
        boss.y += bossDirectionY * bossSpeed * Gdx.graphics.getDeltaTime();

        if (boss.x < 0) {
            boss.x = 0;
            bossDirectionX = 1;
        } else if (boss.x > Gdx.graphics.getWidth() - boss.width) {
            boss.x = Gdx.graphics.getWidth() - boss.width;
            bossDirectionX = -1;
        }

        if (boss.y < Gdx.graphics.getHeight() / 2) {
            boss.y = Gdx.graphics.getHeight() / 2;
            bossDirectionY = 1;
        } else if (boss.y > Gdx.graphics.getHeight() - boss.height) {
            boss.y = Gdx.graphics.getHeight() - boss.height;
            bossDirectionY = -1;
        }

        if (MathUtils.random() < 0.01f) {
            bossDirectionX = MathUtils.randomSign();
            bossDirectionY = MathUtils.randomSign();
        }

        moveBossProjectile();
    }

    private void spawnBoss() {
        boss = new Rectangle(
            Gdx.graphics.getWidth() / 2 - bossShip.getWidth() / 2,
            Gdx.graphics.getHeight() - bossShip.getHeight(),
            bossShip.getWidth() * 2,
            bossShip.getHeight() * 2
        );
        bossHealth = 15;
        bossDirectionX = 1;
        bossDirectionY = -1;
    }

    private void moveBossProjectile() {
        if (bossHealth <= 0) {
            isBossActive = false;
            return;
        }

        if (TimeUtils.nanoTime() - lastBossShotTime > 1_000_000_000L) {
            Rectangle bossProjectile = new Rectangle(
                boss.x + boss.width / 2 - projectileEnemies.getWidth() / 2,
                boss.y,
                projectileEnemies.getWidth(),
                projectileEnemies.getHeight()
            );
            bossProjectiles.add(bossProjectile);
            lastBossShotTime = TimeUtils.nanoTime();
            shootSound.play(0.5f);
        }

        for (Iterator<Rectangle> iter = bossProjectiles.iterator(); iter.hasNext();) {
            Rectangle projectile = iter.next();
            projectile.y -= bossProjectileSpeed * Gdx.graphics.getDeltaTime();

            if (collide(projectile.x, projectile.y, projectile.width, projectile.height, player)) {
                explosionSound.play(0.3f);
                life--;
                iter.remove();
                if (life <= 0) {
                    gameOverExplosion.play(0.5f);
                    backgroundMusic.stop();
                    gameOver = true;
                }
            }

            if (projectile.y + projectile.height < 0) {
                iter.remove();
            }
        }
    }

    private boolean collide(float x1, float y1, float width1, float height1, float x2, float y2, float width2, float height2) {
        return x1 < x2 + width2 && x1 + width1 > x2 && y1 < y2 + height2 && y1 + height1 > y2;}

    private boolean collide(float x1, float y1, float width1, float height1, Rectangle r2) {
        return x1 < r2.x + r2.width && x1 + width1 > r2.x && y1 < r2.y + r2.height && y1 + height1 > r2.y;
    }
}
