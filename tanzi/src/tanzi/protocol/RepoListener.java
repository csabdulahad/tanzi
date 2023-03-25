package tanzi.protocol;

import tanzi.staff.MoveRepo;

/**
 * MoveRepo holds the move list of a chess game, and it provides very convenient interfaces to
 * navigate through those moves, add moves, update moves etc. BRHistorian for example also relies
 * on a MoveRepo. Any critical or major changes to a MoveRepo must be properly addressed at the
 * BRHistorian end as well.
 * <p>
 * By extending this listener class, an implementation can listen for changes and RepoListener
 * being an abstract class, an implementor doesn't have to implement for all types of changes
 * made to a MoveRepo.
 */

public abstract class RepoListener {

    /**
     * MoveRepo invokes this when a move has been successfully added to the repo with the
     * index of that added move. Index is usually the very last item in the move list of
     * the repo.
     */
    public void onRepoMoveAdded(int moveIndex) {
    }

    /**
     * If a move can successfully override the repository then MoveRepo notifies the listener
     * by invoking this method with the move index overriding the repo. For example, the
     * BRHistorian can update its records of changes affected by an override.
     */
    public void onRepoMoveOverride(int moveIndex) {
    }

    /**
     * An internal index is used to navigate through the move list within the MoveRepo. When the
     * application directly modifies that internal index by using MoveRepo.setIndexAt() method then
     * the listener is told about this important update.
     */
    public void onRepoMoveIndexSet(int moveIndex) {
    }

    /**
     * The behaviour of a MoveRepo can be dynamically changed by calling MoveRepo.changeRepoType()
     * method. Such change is really sensitive and very useful when the repository can be reused
     * again to speed up the performance. The listener gets notified of repo configuration changes.
     */
    public void onRepoTypeChanged(MoveRepo.Type type) {
    }

    /**
     * A MoveRepo can be reused only if it can allow application to discard its current move-data and
     * reinitialize with a new one. If for example, a BRHistorian is keeping track of the changes of
     * the discarded move list, then invalidation is absolutely necessary for the BRHistorian too.
     * <p>
     * This invocation allows such MoveRepo reliant to keep itself up-to-date.
     */
    public void onRepoInvalided() {
    }

}