package edu.neumont.csc150.model.goblins;

import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.model.misc.AudioPlayer;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.service.Injectable;
import edu.neumont.csc150.service.SoundService;

import java.util.Timer;
import java.util.TimerTask;

public non-sealed class GoblinPaladin extends Goblin {
    private final float SPEED = 0.5f;
    private volatile boolean isAttacking = false;
    private Timer walkAnimation;
    private final AudioPlayer slashingSound;

    public GoblinPaladin(
            Injectable collision,
            Injectable color,
            Injectable config,
            Injectable sound,
            Vector3 position
    ) {
        super(
                collision,
                color,
                config,
                sound,
                "file:assets/images/goblin paladin/Walk 1.png",
                2,
                position
        );

        slashingSound = soundService.create(
                "assets/audio/sound effects/Slashing.wav",
                false
        );
        slashingSound.setVolume(0.8f);
    }

    @Override
    public void update(Vector3 playerPosition) {
        if (!(isInvincible() || isAttacking)) {
            moveTowards(playerPosition);
            attack(playerPosition);
        }

        super.update(playerPosition);
    }

    @Override
    protected void attack(Vector3 playerPosition) {
        if (Vector3.squaredDistance(getPosition(), playerPosition) >= 4)
            return;

        isAttacking = true;
        Thread attackingThread = new Thread(() -> {
            synchronized (this) {
                Vector3 attackAt = new Vector3(playerPosition);
                setImage("file:assets/images/goblin paladin/Attack 1.png");

                try {
                    this.wait(500);
                } catch (InterruptedException _) {}

                SphereCollider attackArea = new SphereCollider(
                        null,
                        collisionService,
                        new RenderLayer[]{RenderLayer.COLLISION_ONLY},
                        null,
                        new CollisionLayer[]{CollisionLayer.PROJECTILE_ENEMY}
                );
                attackArea.setPosition(attackAt);
                attackArea.setRadius(1);
                setImage("file:assets/images/goblin paladin/Attack 2.png");
                slashingSound.play();

                try {
                    this.wait(500);
                } catch (InterruptedException _) {}

                collisionService.removeHitBox(attackArea);
                isAttacking = false;

                Thread.currentThread().interrupt();
            }
        });

        attackingThread.start();
    }

    private void moveTowards(Vector3 playerPosition) {
        if (
            Vector3.squaredDistance(getPosition(), playerPosition) < 4 ||
            !canSeePlayer(playerPosition)
        ) {
            if (walkAnimation != null) {
                walkAnimation.cancel();
                walkAnimation = null;
            }

            setImage("file:assets/images/goblin paladin/Walk 1.png");
            return;
        }

        Vector3 forward = playerPosition.subtract(getPosition());
        setPosition(getPosition().add(forward.scale(SPEED * configService.fixedDeltaTime)));

        if (walkAnimation == null) {
            walkAnimation = new Timer();
            walkAnimation.scheduleAtFixedRate(new TimerTask() {
                private int walkState = 0;

                @Override
                public void run() {
                    String path = "file:assets/images/goblin paladin/";

                    switch (walkState) {
                        case 0 ->
                                setImage(path + "Walk 1.png");
                        case 1 ->
                                setImage(path + "Walk 2.png");
                        case 2 ->
                                setImage(path + "Walk 3.png");
                        case 3 ->
                                setImage(path + "Walk 4.png");
                    }

                    walkState++;
                    if (walkState > 3) walkState = 0;
                }
            }, 0, 250);
        }
    }

    @Override
    protected void damage() {
        super.damage();
        if (walkAnimation != null) {
            walkAnimation.cancel();
            walkAnimation = null;
        }

        setImage("file:assets/images/goblin paladin/Hurt 2.png");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                setImage("file:assets/images/goblin paladin/Hurt 1.png");
            }
        }, 150);
    }
}
