package org.apache.bookkeeper.bookie;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class BufferedChannelTest {
    @BeforeClass
    public static void setupEnv() {
        BufferedChannelUtils.createFile(BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME);
        BufferedChannelUtils.createFile(BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME);
        BufferedChannelUtils.writeOneByteOnFile(BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, (byte) 'c');
    }

    @AfterClass
    public static void clearEnv(){
        BufferedChannelUtils.deleteFile(BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME);
        BufferedChannelUtils.deleteFile(BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME);
        BufferedChannelUtils.deleteFile(BufferedChannelUtils.NON_EXISTING_FILE_NAME);
    }

}
