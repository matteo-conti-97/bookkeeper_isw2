import io.netty.buffer.ByteBuf;
import org.apache.bookkeeper.bookie.DefaultEntryLogger;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(value= Parameterized.class)
public class DefaultEntryLoggerAddEntryTest {
    private ByteBuf entryBuff;
    private long ledgerId;

    @Parameters
    public static Collection<Object> getTestParameters(){
        return Arrays.asList(new Object[][]{
        });
    }

    public DefaultEntryLoggerAddEntryTest( long ledgerId, ByteBuf entryBuff){

        this.ledgerId = ledgerId;
        this.entryBuff = entryBuff;
    }

    @Test
    public void addEntryTest() {
        DefaultEntryLogger defaultEntryLogger = null;
        try {
            defaultEntryLogger = new DefaultEntryLogger(new ServerConfiguration());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore nella creazione di defaultEntryLogger");
        }
    }

}
