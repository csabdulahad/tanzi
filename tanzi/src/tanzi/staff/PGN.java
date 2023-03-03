package tanzi.staff;

import tanzi.exception.InvalidPGNExcep;
import tanzi.model.MoveMeta;

public class PGN {

    public static String generate(String[] moves) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < moves.length; i += 2) {

            int turnIndex = (i / 2) + 1;

            String moveA = moves[i];
            builder.append(turnIndex);
            builder.append(". ");
            builder.append(moveA);

            if (i + 1 < moves.length) {
                builder.append(" ");
                String moveB = moves[i + 1];
                builder.append(moveB);
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static void writeGame(MoveRepo moveRepo, Arbiter arbiter, MoveHistory moveHistory) throws Exception {
        // first clear the board registry; start from a fresh slate
        arbiter.__clearAndSetup();

        while (moveRepo.hasNext()) {
            MoveMeta moveMeta = MoveAnalyzer.analyze(moveRepo.nextMove());
            moveMeta.color = moveRepo.whoseTurn();
            MoveMaker.move(moveMeta, arbiter, moveHistory);
        }
    }

    public static BoardRegistry writeTo(String[] moves, BoardRegistry br) throws Exception {
        MoveRepo moveRepo = MoveRepo.guardedRepo(moves);

        if (br == null) br = new BoardRegistry();

        Arbiter arbiter = new Arbiter(br);

        while (moveRepo.hasNext()) {
            MoveMeta moveMeta = MoveAnalyzer.analyze(moveRepo.nextMove());
            moveMeta.color = moveRepo.whoseTurn();
            try {
                MoveMaker.move(moveMeta, arbiter, null);
            } catch (Exception e) {
                e.printStackTrace();
                throw new InvalidPGNExcep(moveMeta.move);
            }
        }
        return br;
    }

}
