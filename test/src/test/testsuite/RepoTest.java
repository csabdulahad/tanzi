package test.testsuite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tanzi.staff.MoveRepo;

public class RepoTest {

    String game = "e4, e5, Nf3, Nc5";
    MoveRepo repo;

    @Before
    public void setup() {

    }

    @Test
    public void readOnlyRepoTest() throws Exception {
        repo = MoveRepo.of(MoveRepo.Type.READ_ONLY, game);
        Assert.assertFalse("Move can't be able to be added to the guarded repo", repo.add("b5"));
    }

    @Test
    public void freshRepoTest() throws Exception {
        repo = MoveRepo.of(MoveRepo.Type.REPO_GROWING, game);
        repo.override("e6", 1);
        Assert.assertEquals(2, repo.moveCount());
        Assert.assertEquals("e6", repo.currentMove());
    }

    @Test
    public void guardedRepoTest() throws Exception {
        repo = MoveRepo.of(MoveRepo.Type.REPO_GUARDED, game);
        Assert.assertFalse("Can't override move at guarded indexed move", repo.override("e4"));
        Assert.assertTrue("Can't override move at guarded indexed move", repo.add("e4"));
        Assert.assertEquals("e4", repo.moveAt(repo.moveCount() - 1));
    }

}
