package game2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class EnemySpawner {

    private ArrayList<Point> spawnPoints;
    private ArrayList<Enemy> spawnedEnemies;
    private HashMap<Point, Integer> enemiesPerSpawnPoint;
    private final int MAX_ENEMIES = 10, MIN_ENEMIES_PER_POINT = 2, MAX_ENEMIES_PER_POINT = 4;
    private final long MAX_SPAWN_INTERVAL = 1000; // milliseconds
    private long lastSpawnTime, elapsedTime;
    private Random random;

    public EnemySpawner(ArrayList<Point> sp,  long elapsed){
        this.spawnPoints = sp;
        this.random = new Random();
        this.elapsedTime = elapsed;
        spawnedEnemies = new ArrayList<>();
        enemiesPerSpawnPoint = new HashMap<>();
        lastSpawnTime = System.currentTimeMillis();

        for (Point p : sp) enemiesPerSpawnPoint.put(p, 0);
    }

    public void update(){
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= MAX_SPAWN_INTERVAL && spawnedEnemies.size() < MAX_ENEMIES){
            spawnEnemies();
            lastSpawnTime = System.currentTimeMillis();
        }

        for (Enemy e : spawnedEnemies) e.update(elapsedTime);
    }

    public void removeEnemy(Enemy e){
        spawnedEnemies.remove(e);
        for (Point sp: spawnPoints){
            if (sp.x == (int) e.getX() && sp.y == (int) e.getY()){
                enemiesPerSpawnPoint.put(sp, enemiesPerSpawnPoint.get(sp) - 1);
                break;
            }
        }
    }

    private void spawnEnemies(){
        for(Point sp : spawnPoints){
            int currentEnemies = enemiesPerSpawnPoint.get(sp);

            if (currentEnemies < MIN_ENEMIES_PER_POINT && spawnedEnemies.size() < MAX_ENEMIES){
                while (currentEnemies < MIN_ENEMIES_PER_POINT) {
                    spawnEnemy(sp);
                    currentEnemies++;
                }
            }else if (currentEnemies < MAX_ENEMIES_PER_POINT && random.nextFloat() < 0.5f && spawnedEnemies.size() < MAX_ENEMIES){
                // 50% chance a new enemy spawns if MAX_ENEMIES threshold hasn't been reached
                spawnEnemy(sp);
                currentEnemies++;
            }
        }
    }

    private void spawnEnemy(Point sp){
        if (!spawnPoints.isEmpty()){
            Enemy e = new Enemy();
            e.setPosition(sp.x, sp.y);
            e.setVelocityX(0.025f * (random.nextBoolean() ? 1 : -1)); // init random direction
            spawnedEnemies.add(e);
            System.out.println("Enemy spawned at (X,Y): " + sp.x + ", " + sp.y);
        }
        else System.out.println("No spawn points available!");
    }

    public ArrayList<Enemy> getSpawnedEnemies(){
        return spawnedEnemies;
    }

    public int getSpawnedEnemyCount(){
        return spawnedEnemies.size();
    }

}
