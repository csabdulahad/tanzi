package tanzi.app;

import tanzi.model.MoveMeta;

/**
 * GameSound class represent an in-game sound player. Any class implementing GameSoundPlayer
 * can be registered in the Game class which will be called on each move is made to the BR.
 */

public class GameSound {

    private SoundPlayer player;

    public GameSound(SoundPlayer player) {
        this.player = player;
    }

    public void setPlayer(SoundPlayer player) {
        this.player = player;
    }

    public void metaToSound(MoveMeta meta) {
        if (player == null) return;

        if (meta == null) {
            player.invalidMoveSound();
            return;
        }

        if (meta.checkMate) player.checkmateSound();
        else if (meta.check) player.checkSound();
        else if (meta.simpleMove || meta.uniqueName) player.moveSound();
        else if (meta.takes) player.captureSound();
        else if (meta.castle) player.castleSound();
        else if (meta.promotion) player.promotionSound();
        else player.invalidMoveSound();
    }

}
