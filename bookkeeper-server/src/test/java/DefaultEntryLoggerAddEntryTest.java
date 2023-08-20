import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.bookkeeper.bookie.DefaultEntryLogger;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DefaultEntryLoggerAddEntryTest {
    private ByteBuf entryBuff;
    private long ledgerId;

    @Parameters
    public static Collection<Object[]> getTestParameters(){
        return Arrays.asList(new Object[][]{ //expected, ledgerId, entryBuff
                {-1, Unpooled.buffer(1)},
                {0, Unpooled.buffer(1)},
                {0, null},
                {0, Unpooled.buffer(0)}
        });
    }

    public DefaultEntryLoggerAddEntryTest(long ledgerId, ByteBuf entryBuff){
        this.ledgerId = ledgerId;
        this.entryBuff = entryBuff;
    }

    @Test
    public void addEntryTest() {
        DefaultEntryLogger defaultEntryLogger = null;
        System.out.println("LedgerId: " + ledgerId + " entryBuff: " + entryBuff);
        Assert.assertEquals(0, 0);
    }

}
