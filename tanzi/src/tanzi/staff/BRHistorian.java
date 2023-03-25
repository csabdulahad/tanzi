package tanzi.staff;

import tanzi.algorithm.MoveMaker;
import tanzi.model.BRHistory;
import tanzi.model.MoveMeta;
import tanzi.protocol.RepoListener;

import java.util.HashMap;
import java.util.Map;

/**
 * For a game, pgn moves are analyzed and executed to the board registry. This class makes it possible to navigate
 * through these moves easily to perform undo, redo operations.
 * <p>
 * Whenever a decisive change is made to the BR, this class captures that change and stores on a stack. It uses
 * BRChange class wrapped in BRHistory to describe a move change. BRChange thus has information about that change
 * detailing which pieces were involved, where they are now, whether there was any en-passer object available when
 * the change was made etc.
 * <p>
 * <b>BRHistorian extends RepoListener</b>, which seems very opposite to the idea of OOP. However, this was absolutely
 * necessary to have. Because a repository is subject to be changed in many ways which will overcrowd an interface
 * implementation for any application (which is BRHistorian in this case) with uninterested subscriptions. Here
 * BRHistorian can be exclusive of what changes interest it or what doesn't.
 */

public class BRHistorian extends RepoListener {

    private final MoveRepo repo;

    // the BR, which BRHistorian is concerned with
    private final BoardRegistry br;

    private final Map<Integer, BRHistory> historyStack;

    public BRHistorian(MoveRepo repo, BoardRegistry br) {
        this.repo = repo;
        this.br = br;
        historyStack = new HashMap<>();

        repo.setListener(this);
    }

    /**
     * Any particular change can be undone/redone using a zero-based move index
     * by this method. This method makes sure that the requested index is within
     * the bound of the change stack and then calculate the direction, which the
     * navigation goes along.
     * <p>
     * It returns true on success, false otherwise. Throws exception on finding
     * the un-synced states of historian and repo, not being able to make move
     * for change stack cache miss.
     * <p>
     * One thing to note, <b>after
     * successful navigation, it updates the repo move index to specified argument
     * index to match with the repository.</b>
     * <p>
     * If the requested index is already found in the change stack then it just
     * undoes the BR and MoveRepo's current index to the requested point meaning
     * it goes backward in change-history-time.
     * <p>
     * On the other hand, if the requested index has not been played yet, then it
     * simply plays till the requested point using MoveMaker which saves the BR
     * changes at the same time in the historian.
     */
    public boolean goTo(int moveIndex) {
        // confirm that the requested index within the repo bound
        if (moveIndex >= repo.moveCount() || moveIndex < -1) return false;

        int repoIndex = repo.currentIndex();
        int lastChangeIndex = moveIndexOfLastChange();

        // make sure if we have already where the index is requesting to be
        if (moveIndex == repoIndex) return true;

        // let's figure out if we have played up to the index request first.
        boolean alreadyPlayed = moveIndex <= lastChangeIndex;

        if (alreadyPlayed) {
            // based on the motion sign, calculate the step
            boolean forwardMotion = moveIndex > repoIndex;
            int step = forwardMotion ? 1 : -1;


            // We need to adjust the counter from which the undo/redo starts. For redo, it is simple
            // start from the next index, however, for undo, we need to undo the current move then
            // go all the way down to the index INCLUSIVE.
            int i = forwardMotion ? repoIndex + step : repoIndex;

            while (true) {
                if (forwardMotion) {
                    historyFor(i).redo(br);
                    // We check the condition after redo it is because we want to go to the index INCLUSIVE
                    if(i == moveIndex) break;
                }
                else {
                    // Here, we check before undo, it is because we want to reach to the requested index by undoing
                    // the index before it so after undo, we reach the desired index position.
                    if (i == moveIndex) break;
                    historyFor(i).undo(br);
                }
                i += step;
            }

            // We assumed that our change stack is of course valid to undo/redo.
            // So update the internal index pointer for this navigation
            repo.setIndexAt(moveIndex);

        } else {

            // here we need to see how far we can utilize our history change stack before
            // actually playing a move from the repo
            int playUntil = lastChangeIndex - repoIndex;
            if (playUntil > 0) {
                for (int i = repoIndex + 1; i <= lastChangeIndex; i++)
                    historyFor(i).saveAndExecute(this, br);

                // mark in the repo that we have played from the cache up to max
                repo.setIndexAt(lastChangeIndex);
            }

            // here, we know that we need to play moves till the requested index INCLUSIVE.
            for (int i = lastChangeIndex + 1; i <= moveIndex; i++) {
                try {
                    MoveMeta meta = repo.metaAt(i);
                    BRHistory history = MoveMaker.move(meta, br);

                    // if we can't increment the index by playing then throw  exception to rollback.
                    if (history == null) throw new Exception("Can't play using cached change to requested index.");
                    history.saveAndExecute(this, br);

                    // update the internal index pointer for this navigation
                    repo.setIndexAt(i);
                } catch (Exception e) {
                    goTo(repoIndex);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * This method invocation goes one step back with the MoveRepo using
     * BRHistorian.goTo() method. Returns true on success, false otherwise.
     */
    public boolean goBack() throws Exception {
        return goTo(repo.currentIndex() - 1);
    }

    /**
     * This method invocation goes one step ahead with the MoveRepo using
     * BRHistorian.goTo() method. Returns true on success, false otherwise.
     */
    public boolean goAhead() throws Exception {
        return goTo(repo.currentIndex() + 1);
    }

    public BRHistory peekHistory() {
        return historyStack.getOrDefault(historyStack.size() - 1, null);
    }

    public BRHistory historyFor(int moveIndex) {
        return historyStack.getOrDefault(moveIndex, null);
    }

    public int moveIndexOfLastChange() {
        if (historyStack.isEmpty()) return -1;
        return historyStack.get(historyStack.size() - 1).moveIndex();
    }

    /**
     * Saves the BRHistory object with the zero-based index of the move in
     * the history stack.
     */
    public void saveChange(BRHistory history) {
        historyStack.put(history.moveIndex(), history);
    }

    public int numOfChanges() {
        return historyStack.size();
    }

    @Override
    public void onRepoInvalided() {
        historyStack.clear();
    }

    /**
     * Change stack needs to update and discard those changes which are not valid anymore because of
     * the recent change in the repository. This method loops through the change stack and keep
     * removing the changes up to the overridden index inclusive so that next move can be played by
     * the MoveMaker and the move maker can save changes relating to that move.
     */
    @Override
    public void onRepoMoveOverride(int moveIndex) {
        for (int i = moveIndexOfLastChange(); i >= moveIndex; i--)
            historyStack.remove(i);
    }

}
