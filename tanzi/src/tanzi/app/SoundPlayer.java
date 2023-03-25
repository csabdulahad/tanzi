package tanzi.app;

/**
 * Each move is executed to BR uses MoveMeta object containing useful information which
 * can be used to play different sound to output the game move via audio.
 */

public interface SoundPlayer {

    void moveSound();

    void captureSound();

    void castleSound();

    void promotionSound();

    void checkSound();

    void checkmateSound();

    void invalidMoveSound();

}
