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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class BufferedChannelWriteTest extends BufferedChannelTest{
    String filename;
    Object expected;
    ByteBufAllocator allocator;
    ByteBuf src; //Buffer con i dati da scrivere -> {null}, {buffDim= 0}, {buffDim = 1 empty}, {buffDim = 1, 1Byte data}
    int writeCapacity; //Dimensione buffer di scrittura -> {0, 1}
    FileChannel fc; // FileChannel del file su cui scrivere {null}, {fc empty existing file}, {fc !empty existing file}
    long unpersistedBytesBound; //Parametro aggiunto a seguito dell'evoluzione dei casi di test, indica quanti byte possono essere scritti nel buffer prima di effettuare un flush. {srcBuffDataSize, srcBuffDataSize+1}

    /*@Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() throws FileNotFoundException {
        return Arrays.asList(new Object[][]{ //srcBuff, writeCapacity, fc, filename, expected

                //0 - srcBuff null, 1, fc empty existing file -> Error null srcBuff -> NullPointerException
                {null, 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new NullPointerException()},

                //1 - srcBuffDim=0, 1, fc empty existing file -> Error srcBuffDim 0 -> Stringa vuota
                {Unpooled.buffer(0), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, ""},

                //2 - srcBuffDim=1 empty, 1, fc empty existing file -> Ok -> Stringa vuota
                {Unpooled.buffer(1), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, ""},

                //3 - srcBuffDim=1 1Byte data, 1, fc empty existing file -> Ok -> Stringa "a"
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "a"},

                //4 - arcBuffDim=1 1Byte data, 0, fc empty existing file -> Error writeCapacity 0 -> IllegalArgumentException -> Entra in loop infinito è un bug
                //{Unpooled.buffer(1).writeByte((byte) 'a'), 0, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new IllegalArgumentException()},

                //5 - srcBuffDim=1 1Byte data, 1, null -> Error null fc -> NullPointerException
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, null, null, new NullPointerException()},

                //6 - srcBuffDim=1 1Byte data, 1, fc existing file contenente 1 byte -> Ok -> Stringa "ca"
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "ca"},


        });
    }*/

    @Parameterized.Parameters
    public static Collection<Object[]> getEvolutedTestParameters() throws FileNotFoundException {
        return Arrays.asList(new Object[][]{ //srcBuff, writeCapacity, fc, unpersistedBytesBound, filename, expected

               //0 - srcBuff null, 1, fc empty existing file, unpersistedBytesBound 0  -> Error null srcBuff -> NullPointerException
                {null, 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new NullPointerException()},

                //1 - srcBuffDim=0, 1, fc empty existing file, unpersistedBytesBound 0  -> Error srcBuffDim 0 -> Stringa vuota
                {Unpooled.buffer(0), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, ""},

                //2 - srcBuffDim=1 empty, 1, fc empty existing file, unpersistedBytesBound 0  -> Ok -> Stringa vuota
                {Unpooled.buffer(1), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, ""},

                //3 - srcBuffDim=1 1Byte data, 1, fc empty existing file, unpersistedBytesBound 0  -> Ok -> Stringa "a"
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "a"},

                //4 - arcBuffDim=1 1Byte data, 0, fc empty existing file, unpersistedBytesBound 0  -> Error writeCapacity 0 -> IllegalArgumentException -> Entra in loop infinito è un bug
                //{Unpooled.buffer(1).writeByte((byte) 'a'), 0, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, new IllegalArgumentException()},

                //5 - srcBuffDim=1 1Byte data, 1, null -> Error null fc, unpersistedBytesBound 0  -> NullPointerException
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, null, 0L, null, new NullPointerException()},

                //6 - srcBuffDim=1 1Byte data, 1, fc existing file contenente 1 byte, unpersistedBytesBound 0 -> Ok -> Stringa "ca"
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "ca"},

                //7 - srcBuffDim=1 1Byte data, 1, fc existing file contenente 1 byte, unpersistedBytesBound 1 -> Ok -> Stringa "ca"
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +   BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1L, BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "ca"},

                //8 - srcBuffDim=1 1Byte data, 1, fc empty existing file, unpersistedBytesBound 1 -> Ok -> Stringa "a"
                {Unpooled.buffer(1).writeByte((byte) 'a'), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "a"},

                // EVO 2 -> Casi di test aggiunti a seguito dell'evoluzione 2 dei test, vengono dati in input src contenenti 2byte
                //9 - srcBuffDim=2 2Byte data, 1, fc empty existing file, unpersistedBytesBound 0 -> Ok -> Stringa "ab"
                {Unpooled.buffer(2).writeBytes(new byte[] {(byte)'a', (byte)'b'}), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "ab"},

                //10 - srcBuffDim=2 2Byte data, 1, fc !empty existing file, unpersistedBytesBound 0 -> Ok -> Stringa "cab"
                {Unpooled.buffer(2).writeBytes(new byte[] {(byte)'a', (byte)'b'}), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 0L, BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "cab"},

                //9 - srcBuffDim=2 2Byte data, 1, fc empty existing file, unpersistedBytesBound 1 -> Ok -> Stringa "ab"
                {Unpooled.buffer(2).writeBytes(new byte[] {(byte)'a', (byte)'b'}), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "ab"},

                //10 - srcBuffDim=2 2Byte data, 1, fc !empty existing file, unpersistedBytesBound 1 -> Ok -> Stringa "cab"
                {Unpooled.buffer(2).writeBytes(new byte[] {(byte)'a', (byte)'b'}), 1, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1L, BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "cab"},
                // EVO 3 -> Casi di test aggiunti a seguito dell'evoluzione 3 dei test, vengono dati in input src contenenti 2byte e messa la write capacity a srcDataSize+1=3
                //11 - srcBuffDim=2 2Byte data, 2, fc empty existing file, unpersistedBytesBound 1 -> Ok -> Stringa "ab"
                //{Unpooled.buffer(2).writeBytes(new byte[] {(byte)'a', (byte)'b'}), 3, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1L, BufferedChannelUtils.EMPTY_EXISTING_FILE_NAME, "ab"},

                //11 - srcBuffDim=2 2Byte data, 2, fc !empty existing file, unpersistedBytesBound 1 -> Ok -> Stringa "cab"
                //{Unpooled.buffer(2).writeBytes(new byte[] {(byte)'a', (byte)'b'}), 3, new RandomAccessFile(new File(BufferedChannelUtils.ROOT_DIR_PATH, BufferedChannelUtils.PATH_PREFIX +  BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME), "rw").getChannel(), 1L, BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME, "cab"},

        });
    }


    /*public BufferedChannelWriteTest(ByteBuf src, int writeCapacity, FileChannel fc, String filename, Object expected){
        this.allocator = UnpooledByteBufAllocator.DEFAULT;
        this.src = src;
        this.writeCapacity = writeCapacity;
        this.fc = fc;
        this.filename = filename;
        this.expected = expected;
    }*/

    // Nuovo costruttore, definito a seguito dell'evoluzione dei casi di test per supportare il parametro upersistedBytesBound
    public BufferedChannelWriteTest(ByteBuf src, int writeCapacity, FileChannel fc, long unpersistedBytesBound, String filename, Object expected){
        this.allocator = UnpooledByteBufAllocator.DEFAULT;
        this.src = src;
        this.writeCapacity = writeCapacity;
        this.fc = fc;
        this.unpersistedBytesBound = unpersistedBytesBound;
        this.filename = filename;
        this.expected = expected;
    }

    @Test
    public void writeTest(){
        BufferedChannel bc;
        try {
            //Se il file è non vuoto va settata la pos del fileChannel a 1
            if(filename.equals(BufferedChannelUtils.NON_EMPTY_EXISTING_FILE_NAME)) fc.position(1);
            //bc = new BufferedChannel(allocator, fc, writeCapacity);
            // Nuova istanziazione di BufferedChannel, definita a seguito dell'evoluzione dei casi di test per supportare il parametro upersistedBytesBound
            bc = new BufferedChannel(allocator, fc, writeCapacity, writeCapacity, unpersistedBytesBound);
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
