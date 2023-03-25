package test.testsuite;

import database.Environment;
import org.junit.Before;
import org.junit.Test;
import tanzi.pool.meta.BRMeta;
import tanzi.pool.meta.PieceMeta;
import tanzi.staff.*;

public class ZLogPoolTest {

    @Test
    public void logPool() {
        BRMeta.log();
        PieceMeta.log();
    }


}