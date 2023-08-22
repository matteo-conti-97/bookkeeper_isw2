package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.stats.StatsLogger;
import org.apache.bookkeeper.util.DiskChecker;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.bookkeeper.bookie.BookKeeperServerStats.LD_LEDGER_SCOPE;

@RunWith(Parameterized.class)
public class EntryLogManagerForSingleEntryLogAddEntryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryLogManagerForSingleEntryLogAddEntryTest.class);
    private ByteBuf entryBuff;
    private long ledgerId;
    private long expected;

    @Parameters
    public static Collection<Object[]> getTestParameters(){
        return Arrays.asList(new Object[][]{ //expected, ledgerId, entryBuff
                {0, -1, Unpooled.buffer(1)}, //Mi aspetto un errore per invalid ledger id
                {0, 0, Unpooled.buffer(1)}, //Mi aspetto che mi ritorni la posizione 0 perche crea il log appositamente
                {0, 0, null},
                {0, 0, Unpooled.buffer(0)} //Errore?
        });
    }

    public EntryLogManagerForSingleEntryLogAddEntryTest(long expected, long ledgerId, ByteBuf entryBuff){
        this.ledgerId = ledgerId;
        this.entryBuff = entryBuff;
        this.expected = expected;
    }

    @Test
    public void addEntryTest() {}

}
