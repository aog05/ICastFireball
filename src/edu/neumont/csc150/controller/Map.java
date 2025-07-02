package edu.neumont.csc150.controller;

import edu.neumont.csc150.model.Muffin;
import edu.neumont.csc150.model.SirHoly;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.model.enums.SceneType;
import edu.neumont.csc150.model.goblins.Goblin;
import edu.neumont.csc150.model.Wall;
import edu.neumont.csc150.model.colliders.SpriteCollider;
import edu.neumont.csc150.model.goblins.GoblinPaladin;
import edu.neumont.csc150.model.goblins.GoblinRanger;
import edu.neumont.csc150.model.misc.Quadtree;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.service.*;

import java.awt.Rectangle;
import java.util.*;

public final class Map {
    private static final Random rand = new Random();
    private static final Quadtree quadtree = new Quadtree(1, new Rectangle(-75, -75, 150, 150));
    private static Wall[] walls = new Wall[0];
    private static HitBox[] triggerColliders = new HitBox[0];
    private static SpriteCollider[] images = new SpriteCollider[0];
    private static final List<Goblin> goblins = new LinkedList<>();
    private static final Set<Muffin> muffins = new LinkedHashSet<>();
    private static SirHoly sirHoly;
    private static CollisionService collisionService;
    private static ColorService colorService;
    private static ConfigService configService;
    private static SceneService sceneService;
    private static SoundService soundService;
    private static boolean loadingLevel = false;

    private Map() {
        throw new IllegalStateException("Utility class");
    }

    public static void clearLevel() {
        loadingLevel = true;
        quadtree.clear();
        for (Wall wall : walls)
            collisionService.removeHitBox(wall.getBoxCollider());

        for (SpriteCollider image : images)
            collisionService.removeHitBox(image);

        synchronized (goblins) {
            LinkedList<Goblin> snapshot = new LinkedList<>(goblins);
            snapshot.forEach(Goblin::kill);
            goblins.clear();
        }

        synchronized (muffins) {
            LinkedList<Muffin> snapshot = new LinkedList<>(muffins);
            snapshot.forEach(Muffin::removeMuffin);
            muffins.clear();
        }

        for (HitBox trigger : triggerColliders)
            collisionService.removeHitBox(trigger);

        if (sirHoly != null) {
            sirHoly.unload();
            sirHoly = null;
        }
    }

    public static void loadLevel(SceneType level) {
        clearLevel();

        switch (level) {
            case FIRST_FLOOR -> {
                walls = new Wall[]{
                        new Wall(collisionService, new Vector3(0, 0, -2.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-4.5f, 0, 2), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(4.5f, 0, 2), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(7.893f, 0, 10.168f), 45, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-7.893f, 0, 10.168f), -45, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-11.286f, 0, 18.354f), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(11.286f, 0, 18.354f), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-7.893f, 0, 26.532f), 45, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(7.893f, 0, 26.532f), -45, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-4.5f, 0, 38.7f), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(4.5f, 0, 38.7f), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(-4.5f, 0, 30.7f), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(4.5f, 0, 30.7f), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(-12, 0, 38.2f), 0, new Vector3(16, 10, 1)),
                        new Wall(collisionService, new Vector3(19, 0, 38.2f), 0, new Vector3(30, 10, 1)),
                        new Wall(collisionService, new Vector3(-9.5f, 0, 31.2f), 0, new Vector3(11, 10, 1)),
                        new Wall(collisionService, new Vector3(-23.143f, 0, 34.832f), -135, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-17.663f, 0, 28.312f), -135, new Vector3(8.5f, 10, 1)),
                        new Wall(collisionService, new Vector3(-29.493f, 0, 34.832f), -225, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-17.119f, 0, 22.458f), -225, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-20.654f, 0, 12.572f), -315, new Vector3(20, 10, 1)),
                        new Wall(collisionService, new Vector3(-39.392f, 0, 31.31f), -315, new Vector3(20, 10, 1)),
                        new Wall(collisionService, new Vector3(-36.744f, 0, 15.254f), -45, new Vector3(27.5f, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, 40.2f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(9, 0, 31.2f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(33.5f, 0, 23.9f), 90, new Vector3(30, 10, 1)),
                        new Wall(collisionService, new Vector3(13.5f, 0, 20.15f), 90, new Vector3(23.1f, 10, 1)),
                        new Wall(collisionService, new Vector3(23.5f, 0, 9.2f), 0, new Vector3(21, 10, 1)),
                        new Wall(collisionService, new Vector3(23.75f, 0, 22), 45, new Vector3(4, 10, 4))
                };

                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(-33.5f, 0, 17.5f)));
                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(-40, 0, 24)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(-18, 0, 18)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(-32, 0, 33)));

                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(16.5f, 0, 12)));
                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(30.5f, 0, 12)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(30.5f, 0, 27)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(23.5f, 0, 27)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(16.5f, 0, 27)));

                int randomGoblin = rand.nextInt(goblins.size());
                goblins.get(randomGoblin).hasMuffin = true;
            }
            case SECOND_FLOOR -> {
                walls = new Wall[]{
                        new Wall(collisionService, new Vector3(4.5f, 0, 2), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-4.5f, 0, 2), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, -2.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-9, 0, 6.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(9, 0, 6.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-13.5f, 0, 9), 90, new Vector3(6, 10, 1)),
                        new Wall(collisionService, new Vector3(13.5f, 0, 23), 90, new Vector3(6, 10, 1)),
                        new Wall(collisionService, new Vector3(9, 0, 25.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-4.5f, 0, 26.5f), 90, new Vector3(3, 10, 1)),
                        new Wall(collisionService, new Vector3(4.5f, 0, 26.5f), 90, new Vector3(3, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, 27.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-9, 0, 25.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-19, 0, 20.5f), 0, new Vector3(12, 10, 1)),
                        new Wall(collisionService, new Vector3(-14.5f, 0, 11.5f), 0, new Vector3(3, 10, 1)),
                        new Wall(collisionService, new Vector3(-13.5f, 0, 23), 90, new Vector3(6, 10, 1)),
                        new Wall(collisionService, new Vector3(14.5f, 0, 11.5f), 0, new Vector3(3, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, -6.5f), 0, new Vector3(25, 10, 1)),
                        new Wall(collisionService, new Vector3(13.5f, 0, 9), 90, new Vector3(6, 10, 1)),
                        new Wall(collisionService, new Vector3(-15.5f, 0, 7), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-24.5f, 0, 11.5f), 90, new Vector3(19, 10, 1)),
                        new Wall(collisionService, new Vector3(-29.5f, 0, 2.5f), 0, new Vector3(11, 10, 1)),
                        new Wall(collisionService, new Vector3(-14.5f, 0, 2.5f), 0, new Vector3(3, 10, 1)),
                        new Wall(collisionService, new Vector3(-34.5f, 0, -5.5f), 90, new Vector3(17, 10, 1)),
                        new Wall(collisionService, new Vector3(-12.5f, 0, -2), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, -13.5f), 0, new Vector3(70, 10, 1)),
                        new Wall(collisionService, new Vector3(19, 0, 20.5f), 0, new Vector3(12, 10, 1)),
                        new Wall(collisionService, new Vector3(15.5f, 0, 7), 90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(24.5f, 0, 11.5f), 90, new Vector3(19, 10, 1)),
                        new Wall(collisionService, new Vector3(29.5f, 0, 2.5f), 0, new Vector3(11, 10, 1)),
                        new Wall(collisionService, new Vector3(29.5f, 0, 2.5f), 0, new Vector3(11, 10, 1)),
                        new Wall(collisionService, new Vector3(14.5f, 0, 2.5f), 0, new Vector3(3, 10, 1)),
                        new Wall(collisionService, new Vector3(34.5f, 0, -5.5f), 90, new Vector3(17, 10, 1)),
                        new Wall(collisionService, new Vector3(12.5f, 0, -2), 90, new Vector3(10, 10, 1))
                };

                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(-14.5f, 0, 16)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(14.5f, 0, 16)));
                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(-31, 0, -10)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(-31, 0, -1)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(-24, 0, -10)));
                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(-16, 0, -10)));
                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(31, 0, -10)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(31, 0, -1)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(24, 0, -10)));
                goblins.add(new GoblinRanger(collisionService, colorService, configService, soundService, new Vector3(16, 0, -10)));
                goblins.add(new GoblinPaladin(collisionService, colorService, configService, soundService, new Vector3(0, 0, -10)));

                int randomGoblin = rand.nextInt(goblins.size());
                goblins.get(randomGoblin).hasMuffin = true;
            }
            case GALLERY -> {
                walls = new Wall[]{
                        new Wall(collisionService, new Vector3(4.5f, 0, -1), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(-4.5f, 0, -1), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, -2.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-19, 0, -0.5f), 0, new Vector3(30, 10, 1)),
                        new Wall(collisionService, new Vector3(19, 0, -0.5f), 0, new Vector3(30, 10, 1)),
                        new Wall(collisionService, new Vector3(-33.5f, 0, 6.5f), 90, new Vector3(15, 10, 1)),
                        new Wall(collisionService, new Vector3(33.5f, 0, 6.5f), 90, new Vector3(15, 10, 1)),
                        new Wall(collisionService, new Vector3(-4.5f, 0, 14), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(4.5f, 0, 14), 90, new Vector3(2, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, 15.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(19, 0, 13.5f), 0, new Vector3(30, 10, 1)),
                        new Wall(collisionService, new Vector3(-19, 0, 13.5f), 0, new Vector3(30, 10, 1)),
                        new Wall(collisionService, new Vector3(-30, 0, 10), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(-30, 0, 3), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(-20, 0, 10), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(-20, 0, 3), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(-10, 0, 10), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(-10, 0, 3), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(30, 0, 10), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(30, 0, 3), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(20, 0, 10), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(20, 0, 3), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(10, 0, 10), 0, new Vector3(1, 10, 1)),
                        new Wall(collisionService, new Vector3(10, 0, 3), 0, new Vector3(1, 10, 1))
                };

                images = new SpriteCollider[]{
                        new SpriteCollider(collisionService, colorService, new Vector3(-25, 0, 12.95f), 0, new Vector3(6, 6, 0), "file:assets/images/gallery/jimbo.jpg"),
                        new SpriteCollider(collisionService, colorService, new Vector3(-15, 0, 12.95f), 0, new Vector3(6, 6, 0), "file:assets/images/gallery/beardall.png"),
                        new SpriteCollider(collisionService, colorService, new Vector3(-25, 0, 0.05f), 180, new Vector3(6, 6, 0), "file:assets/images/gallery/Horse Connoisseur.jpg"),
                        new SpriteCollider(collisionService, colorService, new Vector3(-15, 0, 0.05f), 180, new Vector3(6, 6, 0), "file:assets/images/gallery/Master Sword.jpg"),
                        new SpriteCollider(collisionService, colorService, new Vector3(25, 0, 12.95f), 0, new Vector3(6, 6, 0), "file:assets/images/gallery/Taco Dog.jpg"),
                        new SpriteCollider(collisionService, colorService, new Vector3(15, 0, 12.95f), 0, new Vector3(6, 6, 0), "file:assets/images/gallery/Windsor Castle.jpg"),
                        new SpriteCollider(collisionService, colorService, new Vector3(25, 0, 0.05f), 180, new Vector3(6, 6, 0), "file:assets/images/gallery/Arpeggio.png"),
                        new SpriteCollider(collisionService, colorService, new Vector3(15, 0, 0.05f), 180, new Vector3(6, 6, 0), "file:assets/images/gallery/Animal Well.jpg"),
                        new SpriteCollider(collisionService, colorService, new Vector3(0, 0, 14.95f), 0, new Vector3(6, 12, 0), "file:assets/images/gallery/Archway.png"),
                };

                SphereCollider doorTrigger = new SphereCollider(
                        null,
                        collisionService,
                        new RenderLayer[]{RenderLayer.COLLISION_ONLY},
                        new CollisionLayer[]{CollisionLayer.PLAYER},
                        new CollisionLayer[]{CollisionLayer.TRIGGER}
                );
                doorTrigger.setRadius(4);
                doorTrigger.setPosition(new Vector3(0, 0, 12));
                triggerColliders = new HitBox[]{
                    doorTrigger
                };
            }
            case BOSS_ROOM -> {
                walls = new Wall[]{
                        new Wall(collisionService, new Vector3(0, 0, -2.5f), 0, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-8.95f, 0, -0.05f), -30, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-15.45f, 0, 6.45f), -60, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-17.95f, 0, 15.45f), -90, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-15.45f, 0, 24.45f), -120, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(-8.95f, 0, 30.95f), -150, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(0, 0, 33.45f), -180, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(8.95f, 0, 30.95f), -210, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(15.45f, 0, 24.45f), -240, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(17.95f, 0, 15.45f), -270, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(15.45f, 0, 6.45f), -300, new Vector3(10, 10, 1)),
                        new Wall(collisionService, new Vector3(8.95f, 0, -0.05f), -330, new Vector3(10, 10, 1))
                };

                sirHoly = new SirHoly(
                        new Vector3(0, 0, 15.475f),
                        collisionService,
                        colorService,
                        configService,
                        soundService
                );
            }
        }

        for (Wall wall : walls)
            quadtree.insert(wall.getBoxCollider());

        loadingLevel = false;
    }

    public static List<Goblin> getGoblins() {
        return goblins;
    }

    public static void removeGoblin(Goblin goblin) {
        synchronized (goblins) {
            goblins.remove(goblin);

            if (goblins.isEmpty() && !loadingLevel) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sceneService.changeScene(SceneType.INTERMISSION);
                    }
                }, 2000);
            }
        }
    }

    public static void addMuffin(Muffin muffin) {
        muffins.add(muffin);
    }

    public static Set<Muffin> getMuffins() {
        return muffins;
    }

    public static void removeMuffin(Muffin muffin) {
        synchronized (muffins) {
            muffins.remove(muffin);
        }
    }

    public static List<HitBox> getWalls(HitBox checkOn) {
        List<HitBox> foundWalls = new LinkedList<>();
        quadtree.retrieve(foundWalls, checkOn);

        return foundWalls;
    }

    public static HitBox[] getTriggerColliders() {
        return triggerColliders;
    }

    public static SirHoly getSirHoly() {
        return sirHoly;
    }

    public static void injectCollisionService(Injectable collision) {
        collisionService = (CollisionService) collision;
    }

    public static void injectColorService(Injectable color) {
        colorService = (ColorService) color;
    }

    public static void injectConfigService(Injectable config) {
        configService = (ConfigService) config;
    }

    public static void injectSceneService(Injectable scene) {
        sceneService = (SceneService) scene;
    }

    public static void injectSoundService(Injectable sound) {
        soundService = (SoundService) sound;
    }
}
