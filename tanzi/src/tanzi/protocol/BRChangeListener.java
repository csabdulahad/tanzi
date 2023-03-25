package tanzi.protocol;

import tanzi.model.Piece;

/**
 * A BoardRegistry or BR for short holds the game in the memory. Changes to the BR can
 * be observed and any application can listener to it by implementing this interface.
 * Update are only pushed from the BR to listener when the change reflection is not
 * paused. If any part of the application pause the reflection, then follow-up resume
 * reflection methods must be called otherwise listener won't get any update.
 */

public interface BRChangeListener {

    /**
     * Any change involved in deleting a piece from the BR, will be notified to the listener
     * by calling this method.
     *
     * @param square The square the piece was removed from.
     * @param piece  A cloned version of the removed piece. This piece should be recycled after
     *               consumption.
     */
    void onPieceDeletedFromBR(String square, Piece piece);

    /**
     * Any piece addition to the BR will be told to the BR change listener by this method.
     *
     * @param square The square where the piece has been added to.
     * @param piece  A cloned version of the added piece. This piece should be recycled after
     *               consumption.
     */
    void onPieceAddedToBR(String square, Piece piece);

    /**
     * When there have been a lot of changes made to the BR then any dependent on the BR must
     * discard all the previous cache and update itself with the fresh state of the BR.
     */
    void onInvalidation();

    /**
     * A BR can be reloaded with a new game, or it can be recycled. Upon these operation, the BR
     * gets cleared and any observer depending on the BR should be informed about. The BR calls
     * this when it gets cleared.
     */
    void onBRClear();

}
