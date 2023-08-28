package org.apache.bookkeeper.bookie;

import io.netty.buffer.*;
import io.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
public class BufferedChannelReadTest extends BufferedChannelTest{
    //ByteBuf dest, long pos, int lenght, int readCapacity, ByteBuf writeBuffer, FileChannel fc -> Ritorna il numero di byte letti, -1 se la pos indicata supera la taglia del file.
    String filename;
    Object expected;
    ByteBufAllocator allocator;
    ByteBuf dest; //{null}, {buffer di dimensione =0}, {buffer di dimensione = 1}
    long pos; //{-1, fileSize-1, fileSize}
    int lenght; //{destSize, destSize+1}
    int rwCapacity; //{0, 1}
    ByteBuf writeBuffer; //{null}, {buffer vuoto}, {buffer contenente un solo byte}
    FileChannel fc; //{null}, {fc empty existing file}, {fc existing file contenente 1 byte}, {fc !existing file}
    boolean emptyWriteBuffFlag; //{true buffer vuoto}, {false buffer non vuoto}

    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() throws FileNotFoundException {
        return Arrays.asList(new Object[][]{ // allocator, dest, pos, readCapacity, emptyWriteBuffFlag fc, filename, expected

                //0 - allocator, dest null, pos 0, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error null destBuff -> NullPointerException
                {UnpooledByteBufAllocator.DEFAULT, null, 0, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, new NullPointerException()},

                //1 - allocator, destBuffSize 0,  pos 0, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error destBuffSize 0 -> 0 byte letti, la stringa vuota
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(0), 0, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, ""},

                //2 - allocator, destBuffSize 1,  pos -1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error pos -1 -> IllegalArgumentException
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), -1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, new IllegalArgumentException()},

                //3 - allocator, destBuffSize 1,  pos 0, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Ok -> 1 byte letto, la stringa "c"
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "c"},

                //4 - allocator, destBuffSize 1,  pos 0, readCapacity 1, emptyWriteBuff, null fc -> Error null fc -> NullPointerException
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 1, true, null, null, new NullPointerException()},

                //5 - allocator, destBuffSize 1,  pos 1, readCapacity 1, !emptyWriteBuff, fc !empty existing file -> Ok -> 1 byte letto, la stringa "a"
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, false, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "a"},

                //6 - allocator, destBuffSize 1,  pos 1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error pos 1 >= FileSize -> -1 come da doc
                //{UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, -1},

                //6.1 - allocator, destBuffSize 1,  pos 1, readCapacity 1, emptyWriteBuff, fc !empty existing file -> Error pos 1 >= FileSize -> IOException
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 1, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, new IOException()},

                //7 - allocator, destBuffSize 1,  pos 0, readCapacity 0, emptyWriteBuff, fc !empty existing file -> Error readCapacity 0 -> 0 byte letti, la stringa vuota -> Bug entra in loop
                //{UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 0, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, ""},

                //8 - allocator, destBuffSize 1,  pos 0, readCapacity 1, emptyWriteBuff, fc empty existing file -> Error pos 0 >= FileSize vuoto -> -1 come da doc
                //{UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, -1},

                //8.1 - allocator, destBuffSize 1,  pos 0, readCapacity 1, emptyWriteBuff, fc empty existing file -> Error pos 0 >= FileSize 0 -> IOException
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new IOException()},

                //9 - allocator, destBuffSize 1,  pos 0, readCapacity 1, emptyWriteBuff, fc !existing file -> Error pos 0 >= FileSize perche file creato è vuoto-> -1 come da doc
                //{UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EXISTING_FILE_NAME, -1},

                //9.1 - allocator, destBuffSize 1,  pos 0, readCapacity 1, emptyWriteBuff, fc !existing file -> Error pos 0 >= FileSize perchè il file creato è vuoto-> IOException
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 1, true, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EXISTING_FILE_NAME, new IOException()},

                //10 - allocator, destBuffSize 1,  pos 0, readCapacity 1, !emptyWriteBuff, fc !existing file -> Ok -> 1 byte letto crea il file e ci flusha sopra, la stringa "a"
                {UnpooledByteBufAllocator.DEFAULT, Unpooled.buffer(1), 0, 1, false, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EXISTING_FILE_NAME, "a"},

        });
    }



    public BufferedChannelReadTest(ByteBufAllocator allocator, ByteBuf dest, long pos, int rwCapacity, boolean emptyWriteBuffFlag, FileChannel fc, String filename, Object expected){
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
            //Se il file è non vuoto va settata la pos del fileChannel a 1
            if(filename.equals(BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME)) fc.position(1);
            bc = new BufferedChannel(allocator, fc, rwCapacity);
            if(!emptyWriteBuffFlag) bc.writeBuffer.writeByte((byte)'a');
            bc.read(dest, pos);
            String hexContent = ByteBufUtil.hexDump(dest);
            System.out.println(hexContent);

            // Convert the content of ByteBuf to a UTF-8 string and print it
            String utf8Content = dest.toString(CharsetUtil.UTF_8);
            System.out.println("I have read: " + utf8Content);
            int contentSize = dest.readableBytes();
            System.out.println("Content size: " + contentSize + " bytes");
            Assert.assertEquals(expected, utf8Content);
        } catch (Exception e) {
            Assert.assertEquals(expected.getClass(), e.getClass());
            e.printStackTrace();
        }
    }
}
