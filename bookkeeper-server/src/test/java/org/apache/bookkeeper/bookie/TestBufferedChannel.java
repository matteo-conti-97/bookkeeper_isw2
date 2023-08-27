package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
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
            return Arrays.asList(new Object[][]{ //allocator, srcBuff, writeCapacity, fc, filename, expected

                    // allocator, srcBuff null, 1, fc empty existing file -> Error null srcBuff -> NullPointerException
                    {UnpooledByteBufAllocator.DEFAULT, null, 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new NullPointerException()},

                    // allocator, srcBuffDim=0, 1, fc empty existing file -> Error srcBuffDim 0 -> Filesize 0
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(0), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 0L},

                    // allocator, srcBuffDim=1 empty, 1, fc empty existing file -> Ok -> File size 0
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 0L},

                    // allocator, srcBuffDim=1 1Byte data, 1, fc empty existing file -> Ok file size 1
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 1L},

                    // allocator, srcBuffDim=1 1Byte data, 0, fc empty existing file, Error writeCapacity 0 -> File size 0 -> Entra in loop infinito è un bug
                    //{UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 0, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 0L},

                    // allocator, srcBuffDim=1 1Byte data, 1, null -> Error null fc -> NullPointerException
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 1, null, null, new NullPointerException()},

                    // allocator, srcBuffDim=1 1Byte data, 1, fc existing file contenente 1 byte -> Ok -> Filesize = 2
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 1L},

                    // allocator, srcBuffDim=1 1Byte data, 1, fc !existing file -> Ok -> Filesize = 1
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

    /*@RunWith(Parameterized.class)
    public static class TestBufferedChannelRead{
        //ByteBuf dest, long pos, int lenght, int readCapacity, ByteBuf writeBuffer, FileChannel fc -> Ritorna il numero di byte letti, -1 se la pos indicata supera la taglia del file.
        String filename;
        Object expected;
        ByteBufAllocator allocator;
        ByteBuf dest; //{null}, {buffer di dimensione =0}, {buffer di dimensione = 1}
        long pos; //{-1, fileSize, fileSize+1}
        int lenght; //{destSize, destSize+1}
        int rwCapacity; //{0, 1}
        ByteBuf writeBuffer; //{null}, {buffer vuoto}, {buffer contenente un solo byte}
        FileChannel fc; //{null}, {fc empty existing file}, {fc existing file contenente 1 byte}, {fc !existing file}
        boolean emptyWriteBuffFlag; //{true buffer vuoto}, {false buffer non vuoto}

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
            return Arrays.asList(new Object[][]{ // allocator, dest, pos, readCapacity, emptyWriteBuffFlag fc, filename, expected

                    // allocator, dest null, pos 1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error null destBuff -> NullPointerException
                    {UnpooledByteBufAllocator.DEFAULT, null, 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, new NullPointerException()},

                    // allocator, destBuffSize 0,  pos 1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error destBuffSize 0 -> 0 byte letti
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(0), 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, 0},

                    // allocator, destBuffSize 1,  pos -1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error pos -1 -> IllegalArgumentException
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), -1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, new IllegalArgumentException()},

                    // allocator, destBuffSize 1,  pos 1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Ok -> 1 byte letto
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, 1},

                    // allocator, destBuffSize 1,  pos 1, readCapacity 1, !emptyWriteBuff, fc !empty existing file -> Ok -> 1 byte letto, non legge il nuovo byte ma sempre quello in pos 1
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, false, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, 1},

                    // allocator, destBuffSize 1,  pos 2, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error pos 2 > FileSize -> -1 come da doc
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 2, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, -1},

                    // allocator, destBuffSize 1,  pos 1, readCapacity 0, emptyWriteBuff, fc !empty existing file -> Error readCapacity 0 -> 0 byte letti
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 0, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, 0},

                    // allocator, destBuffSize 1,  pos 1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Ok -> 1 byte letto perche destBuffDim 1 altrimenti li avrebbe letti entrambi
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, 1},

                    // allocator, destBuffSize 1,  pos 1, readCapacity 1, emptyWriteBuff, fc empty existing file -> Ok -> 0 byte letti
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, 0},

                    // allocator, destBuffSize 1,  pos 1, readCapacity 1, emptyWriteBuff, fc !existing file -> Ok -> 0 byte letti crea il file
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EXISTING_FILE_NAME, 0},

                    // allocator, destBuffSize 1,  pos 1, readCapacity 1, !emptyWriteBuff, fc !existing file -> Ok -> 1 byte letto crea il file
                    {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, false, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EXISTING_FILE_NAME, 0},
                    //TODO RIMUOVERE DALLE TUPLE DI TEST IL WRITE BUFFER, PER TESTARE WRITEBUFFER PIENO FARE UNA WRITE PRIMA DELLA READ
            });
        }

        public TestBufferedChannelRead(ByteBufAllocator allocator, ByteBuf dest, long pos, int rwCapacity, boolean emptyWriteBuffFlag, FileChannel fc, String filename, Object expected){
            this.allocator = allocator;//
            this.dest=dest;//
            this.pos=pos;//
            this.rwCapacity=rwCapacity;//
            this.emptyWriteBuffFlag=emptyWriteBuffFlag;
            this.fc = fc; //
            this.filename = filename;//
            this.expected = expected;//
        }

        @Test
        public void readTest(){
            BufferedChannel bc;
            try {
                bc = new BufferedChannel(allocator, fc, rwCapacity);
                if(!emptyWriteBuffFlag) this.writeBuffer.writeByte((byte)'a');
                bc.read(dest, pos);
                String hexContent = ByteBufUtil.hexDump(dest);
                System.out.println(hexContent);

                // Convert the content of ByteBuf to a UTF-8 string and print it
                String utf8Content = dest.toString(CharsetUtil.UTF_8);
                System.out.println("I have read: " + utf8Content);
                int contentSize = dest.readableBytes();
                System.out.println("Content size: " + contentSize + " bytes");
                Assert.assertEquals(expected, contentSize);
            } catch (Exception e) {
                Assert.assertEquals(expected.getClass(), e.getClass());
                e.printStackTrace();
            }
        }
    }*/

}
