package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.apache.bookkeeper.common.allocator.impl.ByteBufAllocatorBuilderImpl;
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
public class BufferedChannelTest {

    @RunWith(Parameterized.class)
    public static class BufferedChannelWriteTest{
        ByteBufAllocator allocator;
        ByteBuf src; //Dati da scrivere {null}, {buffDim= 0}, {buffDim = 1 empty}, {buffDim = 1, 1Byte data}
        int writeCapacity; //Dimensione buffer di scrittura Boundary values -> {0, 1}
        long position; //Dove iniziare a scrivere Boundary values -> {-1, 0}
        FileChannel fc; // File su cui scrivere {null}, {fc empty existing file}, {fc !empty existing file}, {fc !existing file}


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
            return Arrays.asList(new Object[][]{ //allocator, src, writeCapacity, position, fc, expected

                    // allocator, null, 1, 0, fc empty existing file, Error null buff -> NullPointerException
                    {new ByteBufAllocatorBuilderImpl(), null, 1, 0, new RandomAccessFile(new File("../resources", BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), Exception.class},

                    // allocator, buffDim=0, 1, 0, fc empty existing file, Error buffDim 0
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(0), 1, 0, new RandomAccessFile(new File("../resources", BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), },

                    // allocator, buffDim=1 empty, 1, 0, fc empty existing file, Ok -> File size 0
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 1, 0, new RandomAccessFile(new File("../resources", BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0},

                    // allocator, buffDim=1 1Byte data, 1, 0, fc empty existing file, Ok file size 1
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 1, 0, new RandomAccessFile(new File("../resources", BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1},

                    // allocator, buffDim=1 1Byte data, 0, 0, fc empty existing file, Error writeCapacity 0 -> File size 0
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 0, 0, new RandomAccessFile(new File("../resources", BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0},

                    // allocator, buffDim=1 1Byte data, 1, -1, fc empty existing file, Error position -1 -> Index out of bound excepetion
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 1, -1, new RandomAccessFile(new File("../resources", BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), Exception.class},

                    // allocator, buffDim=1 1Byte data, 1, 0, null, Error null fc -> NullPointerException
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 1, 0, null, Exception.class},

                    // allocator, buffDim=1 1Byte data, 1, 0, fc existing file contenente 1 byte, Ok -> Filesize = 1 not ok ha sovrascritto, qui abbiamo log append-only
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 1, 0, new RandomAccessFile(new File("../resources", BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1},

                    // allocator, buffDim=1 1Byte data, 1, 0, fc existing file contenente 1 byte, Ok -> Filesize = 2
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 1, 1, new RandomAccessFile(new File("../resources", BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1},

                    // allocator, buffDim=1 1Byte data, 1, 0, fc !existing file, Ok -> Filesize = 1
                    {new ByteBufAllocatorBuilderImpl(), Unpooled.buffer(1), 1, 0, new RandomAccessFile(new File("../resources", BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), 1},

            });
        }

        public BufferedChannelWriteTest(ByteBufAllocator allocator, ByteBuf src, int writeCapacity, long position, FileChannel fc){
            this.allocator = allocator;
            this.src = src;
            this.writeCapacity = writeCapacity;
            this.position = position;
            this.fc = fc;
        }

        @Test
        public void writeTest(){
            BufferedChannel bc;
            try {
                bc = new BufferedChannel(allocator, fc, writeCapacity);
                bc.write(src);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
