package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class BufferedChannelWriteTest extends BufferedChannelTest{
    String filename;
    Object expected;
    ByteBufAllocator allocator;
    ByteBuf src; //Buffer con i dati da scrivere -> {null}, {buffDim= 0}, {buffDim = 1 empty}, {buffDim = 1, 1Byte data}
    int writeCapacity; //Dimensione buffer di scrittura -> {0, 1}
    FileChannel fc; // FileChannel del file su cui scrivere {null}, {fc empty existing file}, {fc !empty existing file}, {fc !existing file}

    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() throws FileNotFoundException {
        return Arrays.asList(new Object[][]{ //srcBuff, writeCapacity, fc, filename, expected

                //0 - srcBuff null, 1, fc empty existing file -> Error null srcBuff -> NullPointerException
                {null, 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new NullPointerException()},

                //1 - srcBuffDim=0, 1, fc empty existing file -> Error srcBuffDim 0 -> Filesize 0
                {Unpooled.buffer(0), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, ""},

                //2 - srcBuffDim=1 empty, 1, fc empty existing file -> Ok -> File size 0
                {Unpooled.buffer(1), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, ""},

                //3 - srcBuffDim=1 1Byte data, 1, fc empty existing file -> Ok file size 1
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "a"},

                //4 - arcBuffDim=1 1Byte data, 0, fc empty existing file, Error writeCapacity 0 -> File size 0 -> Entra in loop infinito è un bug
                //{Unpooled.buffer(1).writeByte((byte) 'a'), 0, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, ""},

                //5 - srcBuffDim=1 1Byte data, 1, null -> Error null fc -> NullPointerException
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, null, null, new NullPointerException()},

                //6 - srcBuffDim=1 1Byte data, 1, fc existing file contenente 1 byte -> Ok -> Filesize = 2
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "ca"},

                //7 - srcBuffDim=1 1Byte data, 1, fc !existing file -> Ok -> Filesize = 1
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "a"},

        });
    }

    public BufferedChannelWriteTest(ByteBuf src, int writeCapacity, FileChannel fc, String filename, Object expected){
        this.allocator = UnpooledByteBufAllocator.DEFAULT;
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
            //Se il file è non vuoto va settata la pos del fileChannel a 1

            if(filename.equals(BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME)) fc.position(1);
            bc = new BufferedChannel(allocator, fc, writeCapacity);
            bc.write(src);
            String fileContent = BufferedChannelUtils.readFileContent(filename);
            System.out.println("File content: " + fileContent);
            Assert.assertEquals(expected, fileContent);
        } catch (Exception e) {
            Assert.assertEquals(expected.getClass(), e.getClass());
            e.printStackTrace();
        }
    }
}
