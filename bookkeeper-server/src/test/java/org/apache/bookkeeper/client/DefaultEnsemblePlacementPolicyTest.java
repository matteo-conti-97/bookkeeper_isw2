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

import java.nio.charset.StandardCharsets;
import java.util.*;

@RunWith(Parameterized.class)
public class DefaultEnsemblePlacementPolicyTest {
    int ensembleSize; // {-1, 0, 1}
    int writeQuorumSize; // {ensembleSize +1, ensembleSize}
    int ackQuorumSize; // {writeQuorumSize+1, writeQuorumSize}
    Map<String, byte[]> customMetadata; // {null, empty map, map with one element}
    Set<BookieId> excludeBookies; // {null, empty set, set with one element}
    String excludeBookiesType;
    Object expected;



    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(new Object[][]{ // ensebmleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookieSetType, expected
                //0 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set-> IllegalArgumentException for the -1
                {-1, -1, -1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, new IllegalArgumentException()},
                //1 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set -> Valid ensembleSize/writeQuorumSize/ackQuorumSize but 0 -> Ensemble with no bookies
                {0, 0, 0, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, 0},
                //2 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, 1},
                //3 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, null set -> NullPointerException
                {1, 1, 1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.NULL_SET, new NullPointerException()},
                //4 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.NON_EMPTY_SET, 1},
                //5 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set -> Illegal argument exception ackQuorumSize > writeQuorumSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 1, 2, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, new IllegalArgumentException()},
                //6 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set -> Illegal argument exception writeQuorumSize > ensembleSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 2, 1, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, new IllegalArgumentException()},
                //7 - ensembleSize 1, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set -> Ensemble with 1 bookie
                {1, 0, 0, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, 1},
                //8 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set -> Ensemble with 1 bookie
                {1, 1, 0, new HashMap<>(), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, 1},
                //9 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set -> Ensemble with 1 bookie -> no use metadata
                {1, 1, 1, null, DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, 1},
                //10 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), DefaultEnsemblePlacementPolicyUtils.EMPTY_SET, 1}
        });
    }
    public DefaultEnsemblePlacementPolicyTest(int ensembleSize, int writeQuorumSize, int ackQuorumSize, Map<String, byte[]> customMetadata, String excludeBookiesType, Object expected){
        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.customMetadata = customMetadata;
        this.excludeBookiesType = excludeBookiesType;
        this.expected = expected;
    }

    /*@Test
    public void newEnsembleTest(){
        assert(true);
    }*/
    @Test
    public void newEnsembleTest(){
        if(Objects.equals(excludeBookiesType, DefaultEnsemblePlacementPolicyUtils.EMPTY_SET))
            this.excludeBookies = new HashSet<>();
        else if(Objects.equals(excludeBookiesType, DefaultEnsemblePlacementPolicyUtils.NULL_SET))
            this.excludeBookies = null;
        else this.excludeBookies = DefaultEnsemblePlacementPolicyUtils.createDummyHashSet(1);
        EnsemblePlacementPolicy esp;
        EnsemblePlacementPolicy.PlacementResult<List<BookieId>> ensembleBookies;
        try {
            esp = new DefaultEnsemblePlacementPolicy();
            esp.onClusterChanged(DefaultEnsemblePlacementPolicyUtils.createDummyHashSet(7),
                    DefaultEnsemblePlacementPolicyUtils.createDummyHashSet(7));
            ensembleBookies = esp.newEnsemble(ensembleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookies);
            System.out.println("Created " + ensembleBookies.getResult().size() +" bookies.");
            Assert.assertEquals(expected, ensembleBookies.getResult().size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertEquals(expected.getClass(), e.getClass());
        }
    }
}
