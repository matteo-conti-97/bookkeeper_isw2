import io.netty.buffer.ByteBuf;
import org.apache.bookkeeper.bookie.DefaultEntryLogger;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class DefaultEntryLoggerReadEntryTest {
    private long ledgerId;
    private long entryLocation;
    private long entryId;
    private Object expected;


    @Parameterized.Parameters
    public static Collection<Object> getTestParameters(){
        return Arrays.asList(new Object[][]{
        });
    }

    public DefaultEntryLoggerReadEntryTest(Object expected, long entryId, long ledgerId, long entryLocation){
        this.expected = expected;
        this.entryId = entryId;
        this.ledgerId = ledgerId;
        this.entryLocation = entryLocation;
    }

    @Test
    public void readEntryTest() {
        DefaultEntryLogger defaultEntryLogger = null;
        try {
            defaultEntryLogger = new DefaultEntryLogger(new ServerConfiguration());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore nella creazione di defaultEntryLogger");
        }
    }
}
