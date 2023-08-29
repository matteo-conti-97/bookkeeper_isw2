package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
@RunWith(Parameterized.class)
public class BufferedChannelCloseTest extends BufferedChannelTest{
    FileChannel fc;
    Object expected;

    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() throws FileNotFoundException {
        return Arrays.asList(new Object[][]{ // fc, expected

                // null fc -> NullPointerException
                {null, new NullPointerException()},

                // fc empty existing file -> fc.isClosed() = false
                {new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), false},

                // fc non existing file -> Viene creato i file e poi chiuso fc.isCloded() = false
                {new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), false},

        });
    }

    public BufferedChannelCloseTest(FileChannel fc, Object expected) {
        this.fc = fc;
        this.expected = expected;
    }

    @Test
    public void closeTest(){
        BufferedChannel bc;
        try {
            bc = new BufferedChannel(UnpooledByteBufAllocator.DEFAULT, fc, 1);
            bc.close();
            Assert.assertEquals(expected, fc.isOpen());
        } catch (Exception e) {
            Assert.assertEquals(expected.getClass(), e.getClass());
            e.printStackTrace();
        }
    }

}
