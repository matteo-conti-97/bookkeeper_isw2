package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Enclosed.class)
public class TestBufferedChannel {

    @RunWith(Parameterized.class)
    public static class TestBufferedChannelWrite{
        String filename;
        Object expected;
        ByteBufAllocator allocator;
        ByteBuf src; //Buffer con i dati da scrivere -> {null}, {buffDim= 0}, {buffDim = 1 empty}, {buffDim = 1, 1Byte data}
        int writeCapacity; //Dimensione buffer di scrittura -> {0, 1}
        FileChannel fc; // FileChannel del file su cui scrivere {null}, {fc empty existing file}, {fc !empty existing file}, {fc !existing file}


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

        @Parameters
        public static Collection<Object[]> getTestParameters() throws FileNotFoundException {
            return Arrays.asList(new Object[][]{ //allocator, write, src, writeCapacity, fc, filename, expected

                    // allocator, null, 1, fc empty existing file, Error null buff -> NullPointerException
                    {UnpooledByteBufAllocator.DEFAULT, null, 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new NullPointerException()},

                    // allocator, buffDim=0, 1, fc empty existing file, Error buffDim 0 -> Filesize 0
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(0), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 0L},

                    // allocator, buffDim=1 empty, 1, fc empty existing file, Ok -> File size 0
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 0L},

                    // allocator, buffDim=1 1Byte data, 1, fc empty existing file, Ok file size 1
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 1L},

                    // allocator, buffDim=1 1Byte data, 0, fc empty existing file, Error writeCapacity 0 -> File size 0 -> Entra in loop infinito è un bug
                    //{UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 0, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 0L},

                    // allocator, buffDim=1 1Byte data, 1, null, Error null fc -> NullPointerException
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 1, null, null, new NullPointerException()},

                    // allocator, buffDim=1 1Byte data, 1, fc existing file contenente 1 byte, Ok -> Filesize = 2
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 1L},

                    // allocator, buffDim=1 1Byte data, 1, fc !existing file, Ok -> Filesize = 1
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 1L},

            });
        }

        public TestBufferedChannelWrite(ByteBufAllocator allocator, ByteBuf src, int writeCapacity, FileChannel fc, String filename, Object expected){
            this.allocator = allocator;
            this.src = src;
            this.writeCapacity = writeCapacity;
            this.fc = fc;
            this.filename = filename;
            this.expected = expected;
        }

        @Test
        public void writeTest(){
            BufferedChannel bc;
            try {
                bc = new BufferedChannel(allocator, fc, writeCapacity);
                bc.write(src);
                System.out.println("Filename: " + filename);
                long fileSize = BufferedChannelUtils.readFileSize(filename);
                Assert.assertEquals(expected, fileSize);
            } catch (Exception e) {
                Assert.assertEquals(expected.getClass(), e.getClass());
                e.printStackTrace();
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class TestBufferedChannelRead{
        //ByteBuf dest, long pos, int lenght, int readCapacity, ByteBuf writeBuffer, FileChannel fc -> Ritorna il numero di byte letti, -1 se la pos indicata supera la taglia del file.
        Object expected;
        ByteBuf dest;
        long pos;
        int lenght;
        int readCapacity;
        ByteBuf writeBuffer;
        FileChannel fc;
        ByteBufAllocator allocator;

        @Parameters
        public static Collection<Object[]> getTestParameters() throws FileNotFoundException {
            return Arrays.asList(new Object[][]{ // allocator, dest, pos, lenght, readCapacity, writeBuffer, fc, expected

            });
        }
    }

}
