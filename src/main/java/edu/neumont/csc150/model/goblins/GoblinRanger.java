package edu.neumont.csc150.model.goblins;

import edu.neumont.csc150.model.misc.AudioPlayer;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.projectiles.Arrow;
import edu.neumont.csc150.service.Injectable;
import edu.neumont.csc150.service.SoundService;

public non-sealed class GoblinRanger extends Goblin {
    private volatile boolean isAttacking = false;
    private Thread attackingThread = null;
    private final AudioPlayer arrowShotSound;

    public GoblinRanger(
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
                "file:assets/images/goblin ranger/Attack 1.png",
                1,
                position
        );

        arrowShotSound = soundService.create(
                "assets/audio/sound effects/Arrow Shot.wav",
                false
        );
        arrowShotSound.setVolume(0.8f);
    }

    @Override
    public void update(Vector3 playerPosition) {
        if (
            !isAttacking &&
            canSeePlayer(playerPosition) &&
            Vector3.squaredDistance(playerPosition, getPosition()) < 169
        ) attack(playerPosition);

        super.update(playerPosition);
    }

    @Override
    public void kill() {
        if (attackingThread != null && attackingThread.isAlive() && !attackingThread.isInterrupted())
            attackingThread.interrupt();
        super.kill();
    }

    @Override
    protected void attack(Vector3 playerPosition) {
        attackingThread = new Thread(() -> {
            synchronized (this) {
                isAttacking = true;

                try {
                    this.wait(500);
                    setImage("file:assets/images/goblin ranger/Attack 2.png");
                    this.wait(250);
                    setImage("file:assets/images/goblin ranger/Attack 3.png");
                    this.wait(150);
                } catch (InterruptedException _) {}

                new Arrow(position.subtract(new Vector3(0, 1, 0)), playerPosition.subtract(getPosition()).normalize());
                setImage("file:assets/images/goblin ranger/Attack 1.png");
                arrowShotSound.play();
                isAttacking = false;
            }
        });

        attackingThread.start();
    }
}
