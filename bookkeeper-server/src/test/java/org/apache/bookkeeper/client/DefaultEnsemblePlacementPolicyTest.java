package org.apache.bookkeeper.client;

import com.google.common.collect.ImmutableMap;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.discover.RegistrationClient;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.proto.BookieAddressResolver;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.stats.StatsLogger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RunWith(Parameterized.class)
public class DefaultEnsemblePlacementPolicyTest {
    int ensembleSize; // {-1, 0, 1}
    int writeQuorumSize; // {ensembleSize +1, ensembleSize}
    int ackQuorumSize; // {writeQuorumSize+1, writeQuorumSize}
    Map<String, byte[]> customMetadata; // {null, empty map, map with one element}
    Set<BookieId> excludeBookies; // {null, empty set, set with one element}
    boolean isWeighted; //{true, false} aggiunto a seguito della prima evoluzione dei casi di test
    Object expected;



    /*@Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(new Object[][]{ // ensebmleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookieSetType, expected
                //0 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set-> IllegalArgumentException for the -1
                {-1, -1, -1, new HashMap<>(), new HashSet<>(), new IllegalArgumentException()},
                //1 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set -> Valid ensembleSize/writeQuorumSize/ackQuorumSize but 0 -> Ensemble with no bookies
                {0, 0, 0, new HashMap<>(), new HashSet<>(), 0},
                //2 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), new HashSet<>(), 1},
                //3 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, null set -> NullPointerException
                {1, 1, 1, new HashMap<>(), null, new NullPointerException()},
                //4 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.createDummyHashSet(1), 1},
                //5 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set -> Illegal argument exception ackQuorumSize > writeQuorumSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 1, 2, new HashMap<>(), new HashSet<>(), new IllegalArgumentException()},
                //6 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set -> Illegal argument exception writeQuorumSize > ensembleSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 2, 1, new HashMap<>(), new HashSet<>(), new IllegalArgumentException()},
                //7 - ensembleSize 1, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set -> Ensemble with 1 bookie
                {1, 0, 0, new HashMap<>(), new HashSet<>(), 1},
                //8 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set -> Ensemble with 1 bookie
                {1, 1, 0, new HashMap<>(), new HashSet<>(), 1},
                //9 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set -> Ensemble with 1 bookie -> no use metadata
                {1, 1, 1, null, new HashSet<>(), 1},
                //10 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), 1}
        });
    }*/

    //Nuove tuple + tuple vecchie modificate a seguito della prima evoluzione dei casi di test
    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(new Object[][]{ // ensebmleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookieSetType, expected
                /*//0 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set, isWeighted -> IllegalArgumentException for the -1
                {-1, -1, -1, new HashMap<>(), new HashSet<>(), false, new IllegalArgumentException()},
                //1 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, false -> Valid ensembleSize/writeQuorumSize/ackQuorumSize but 0 -> Ensemble with no bookies
                {0, 0, 0, new HashMap<>(), new HashSet<>(), false, 0},
                //2 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, false -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), new HashSet<>(), false, 1},
                //3 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, null set, false -> NullPointerException
                {1, 1, 1, new HashMap<>(), null, false, new NullPointerException()},
                //4 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie, false -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.createDummyHashSet(1), false, 1},
                //5 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set, false -> Illegal argument exception ackQuorumSize > writeQuorumSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 1, 2, new HashMap<>(), new HashSet<>(), false, new IllegalArgumentException()},
                //6 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set, false -> Illegal argument exception writeQuorumSize > ensembleSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 2, 1, new HashMap<>(), new HashSet<>(), false, new IllegalArgumentException()},
                //7 - ensembleSize 1, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, false -> Ensemble with 1 bookie
                {1, 0, 0, new HashMap<>(), new HashSet<>(), false, 1},
                //8 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set, false -> Ensemble with 1 bookie
                {1, 1, 0, new HashMap<>(), new HashSet<>(), false, 1},
                //9 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set, false -> Ensemble with 1 bookie -> no use metadata
                {1, 1, 1, null, new HashSet<>(), false, 1},
                //10 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set, false -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), false, 1},*/

                //EVO 1
                //11 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set true -> IllegalArgumentException for the -1
                {-1, -1, -1, new HashMap<>(), new HashSet<>(), true, new IllegalArgumentException()},
                //12 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set true -> Valid ensembleSize/writeQuorumSize/ackQuorumSize but 0 -> Ensemble with no bookies
                {0, 0, 0, new HashMap<>(), new HashSet<>(), true, 0},
                //13 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set true -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), new HashSet<>(), true, 1},
                //14 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie true -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.createDummyHashSet(1), true, 1},
                //15 - ensembleSize 1, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set true -> Ensemble with 1 bookie
                {1, 0, 0, new HashMap<>(), new HashSet<>(), true, 1},
                //16 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set true -> Ensemble with 1 bookie
                {1, 1, 0, new HashMap<>(), new HashSet<>(), true, 1},
                //17 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set true -> Ensemble with 1 bookie -> no use metadata
                {1, 1, 1, null, new HashSet<>(), true, 1},
                //18 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set true -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), true, 1},
        });
    }


    public DefaultEnsemblePlacementPolicyTest(int ensembleSize, int writeQuorumSize, int ackQuorumSize, Map<String, byte[]> customMetadata, Set<BookieId> excludeBookies, boolean isWeighted, Object expected){
        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.customMetadata = customMetadata;
        this.excludeBookies = excludeBookies;
        this.isWeighted = isWeighted;
        this.expected = expected;
    }

    @Test
    public void newEnsembleTest(){
        EnsemblePlacementPolicy esp;
        EnsemblePlacementPolicy.PlacementResult<List<BookieId>> ensembleBookies;
        try {
            //EVO 1 use reflection to access private attribute isWeighted
            Field isWeightedAttribute = DefaultEnsemblePlacementPolicy.class.getDeclaredField("isWeighted");
            isWeightedAttribute.setAccessible(true);
            Field weightedBookiesAttribute = DefaultEnsemblePlacementPolicy.class.getDeclaredField("weightedSelection");
            weightedBookiesAttribute.setAccessible(true);

            //use reflection to access private attribute knownBookies
            Field knownBookiesAttribute = DefaultEnsemblePlacementPolicy.class.getDeclaredField("knownBookies");
            knownBookiesAttribute.setAccessible(true);
            esp = new DefaultEnsemblePlacementPolicy();

            //EVO 1 set isWeighted attribute with reflection
            isWeightedAttribute.set(esp, isWeighted);
            WeightedRandomSelectionImpl<BookieId> mockedWeightedRandomSelection = Mockito.mock(WeightedRandomSelectionImpl.class);
            Mockito.when(mockedWeightedRandomSelection.getNextRandom()).thenReturn(BookieId.parse("mockedBookie"));
            weightedBookiesAttribute.set(esp, mockedWeightedRandomSelection); //Necessario per non avere nullpointerexception
            //set knownBookies attribute with reflection
            knownBookiesAttribute.set(esp, DefaultEnsemblePlacementPolicyUtils.createDummyHashSet(7));

            ensembleBookies = esp.newEnsemble(ensembleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookies);
            System.out.println("Created " + ensembleBookies.getResult().size() +" bookies.");
            Assert.assertEquals(expected, ensembleBookies.getResult().size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertEquals(expected.getClass(), e.getClass());
        }
    }
}
