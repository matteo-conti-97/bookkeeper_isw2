package org.apache.bookkeeper.client;

import com.google.common.collect.ImmutableMap;
import org.apache.bookkeeper.client.BKException.BKNotEnoughBookiesException;
import org.apache.bookkeeper.client.DefaultEnsemblePlacementPolicyUtils.BookieType;
import org.apache.bookkeeper.net.BookieId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.bookkeeper.client.DefaultEnsemblePlacementPolicyUtils.createDummyHashSet;

@RunWith(Parameterized.class)
public class DefaultEnsemblePlacementPolicyTest {
    int ensembleSize; // {-1, 0, 1}
    int writeQuorumSize; // {ensembleSize +1, ensembleSize}
    int ackQuorumSize; // {writeQuorumSize+1, writeQuorumSize}
    Map<String, byte[]> customMetadata; // {null, empty map, map with one element}
    Set<BookieId> excludeBookies; // {null, empty set, set with one element in known bookies, set with one element not in known bookies}
    boolean isWeighted; //{true, false} aggiunto a seguito della prima evoluzione dei casi di test
    Set<BookieId> knownBookies; //{null, empty set, set with one element} aggiunto a seguito della seconda evoluzione dei casi di test
    WeightedRandomSelection<BookieId> wrsb; // {null, empty map, map with one element in known bookies,  map with one element not in known bookies} aggiunto a seguito della seconda evoluzione dei casi di test
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
                {1, 1, 1, new HashMap<>(), createDummyHashSet(1), 1},
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
                //10 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0,  metadata, empty set -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), 1}
        });
    }*/


    //Nuove tuple + tuple vecchie modificate a seguito della prima evoluzione dei casi di test
    /*@Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(new Object[][]{ // ensebmleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookies, isWeighted, expected
                //0 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set, isWeighted -> IllegalArgumentException for the -1
                {-1, -1, -1, new HashMap<>(), new HashSet<>(), false, new IllegalArgumentException()},
                //1 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, false -> Valid ensembleSize/writeQuorumSize/ackQuorumSize but 0 -> Ensemble with no bookies
                {0, 0, 0, new HashMap<>(), new HashSet<>(), false, 0},
                //2 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, false -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), new HashSet<>(), false, 1},
                //3 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, null set, false -> NullPointerException
                {1, 1, 1, new HashMap<>(), null, false, new NullPointerException()},
                //4 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie, false -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), createDummyHashSet(1), false, 1},
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
                //10 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, metadata, empty set, false -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), false, 1},

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
                //17 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set true -> Ensemble with 1 bookie -> no use metadata
                {1, 1, 1, new HashMap<>(), new HashSet<>(), true, 1},
                //18 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, metadata, empty set true -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), true, 1},
        });
    }*/

    @Parameterized.Parameters //EVO2 per supportare weightedRandomSelection e knownBookies
    public static Collection<Object[]> getTestParameters() {
        return Arrays.asList(new Object[][]{ // ensebmleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookies, isWeighted, knownBookies, weightedRandomSelection, expected
                /*//0 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set, isWeighted, set with 1 bookie, map with 1 known bookie -> IllegalArgumentException for the -1
                {-1, -1, -1, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, new IllegalArgumentException()},
                //1 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Ensemble with no bookies
                {0, 0, 0, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, 0},
                //2 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //3 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, null set, false, set with 1 bookie, map with 1 known bookie -> NullPointerException
                {1, 1, 1, new HashMap<>(), null, false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, new NullPointerException()},
                //4 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie, false, set with 1 bookie, map with 1 known bookie -> Creo ensemble di taglia 1 ma ho 0 bookie -> BKNotEnoughBookiesException
                {1, 1, 1, new HashMap<>(), createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.EMPTY, new BKNotEnoughBookiesException()},
                //5 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Illegal argument exception ackQuorumSize > writeQuorumSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 1, 2, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), WeightedRandomSelectionType.NON_EMPTY_KNOWN, new IllegalArgumentException()},
                //6 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 2, no metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Illegal argument exception writeQuorumSize > ensembleSize -> Bug lui crea tranquillamente l'insieme con 1 bookie
                //{1, 2, 1, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), WeightedRandomSelectionType.NON_EMPTY_KNOWN, new IllegalArgumentException()},
                //7 - ensembleSize 1, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie -> writeQuorum 0
                {1, 0, 0, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //8 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie -> ackQuorum 0
                {1, 1, 0, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //9 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie -> no use metadata
                {1, 1, 1, null, new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //10 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, null metadata, empty set, false, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.EMPTY, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
*/
                //EVO 1
                //11 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set, true, set with 1 bookie, map with 1 known bookie -> IllegalArgumentException for the -1
                //{-1, -1, -1, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, new IllegalArgumentException()},
                //12 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, true, set with 1 bookie, map with 1 known bookie -> Ensemble with no bookies
               // {0, 0, 0, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, 0},
                //13 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, true, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //14 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie known, true, set with 1 bookie, map with 1 known bookie -> BKNotEnoughBookiesException
                {1, 1, 1, new HashMap<>(), createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, new BKNotEnoughBookiesException()},
                //15 - ensembleSize 1, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, true, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie
                {1, 0, 0, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //16 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set, true, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie
                {1, 1, 0, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //17 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, metadata, empty set, true, set with 1 bookie, map with 1 known bookie -> Ensemble with 1 bookie -> use metadata
                {1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},

                //EVO2
                //18 - ensembleSize -1, writeQuorumSize -1, ackQuorumSize -1, no metadata, empty set, true, set with 1 bookie, map with 1 not known bookie -> IllegalArgumentException for the -1
                {-1, -1, -1, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, new IllegalArgumentException()},
                //19 - ensembleSize 0, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, true, set with 1 bookie, map with 1 not known bookie -> Ensemble with no bookies
                {0, 0, 0, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, 0},
                //20 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie kniwb, true, set with 1 bookie, map with 1 not known bookie -> BKNotEnoughBookiesException
                {1, 1, 1, new HashMap<>(), createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, new BKNotEnoughBookiesException()},
                //21 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, true, set with 1 bookie, map with 1 not known bookie -> Ensemble with 1 bookie no metadata -> BUG ? mi crea un ensemble con bookie che in teoria non esiste
                //{1, 1, 1, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //22 - ensembleSize 1, writeQuorumSize 0, ackQuorumSize 0, no metadata, empty set, true, set with 1 bookie, map with 1 not known bookie -> Ensemble with 1 bookie -> BUG ? mi crea un ensemble con bookie che in teoria non esiste
                //{1, 0, 0, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //23 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, no metadata, empty set, true, set with 1 bookie, map with 1 not known bookie -> Ensemble with 1 bookie -> BUG ? mi crea un ensemble con bookie che in teoria non esiste
                //{1, 1, 0, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //24 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 0, metadata, empty set, true, set with 1 bookie, map with 1 not known bookie -> Ensemble with 1 bookie -> use metadata -> BUG ? mi crea un ensemble con bookie che in teoria non esiste
                //{1, 1, 1, ImmutableMap.of("testBookie", "TestMetadata".getBytes(StandardCharsets.UTF_8)), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},

                //25 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, true, set with 1 bookie, null -> NullPointerException
                {1, 1, 1, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NULL, new NullPointerException()},
                //26 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, false, set with 1 bookie, null -> ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), new HashSet<>(), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NULL, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},

                //27 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, set con un bookie not known, false, set with 1 bookie, map with 1 not known bookie -> Ensemble with 1 bookie
                {1, 1, 1, new HashMap<>(), createDummyHashSet(1, BookieType.NON_EMPTY_NON_KNOWN), false, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.NON_EMPTY_NON_KNOWN, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN)},
                //28 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty , false, null, map with 1 not known bookie -> NullPointerException
                {1, 1, 1, new HashMap<>(), new HashSet<>(), false, null, BookieType.NON_EMPTY_NON_KNOWN, new NullPointerException()},
                //29 - ensembleSize 1, writeQuorumSize 1, ackQuorumSize 1, no metadata, empty set, true, set with 1 bookie, empty set -> IllegalArgumentException/NullPointerException -> BUG ? mi crea un ensemble con bookie nullo
                //{1, 1, 1, new HashMap<>(), new HashSet<>(), true, createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), BookieType.EMPTY, new IllegalArgumentException()},

        });
    }

    /*
    public DefaultEnsemblePlacementPolicyTest(int ensembleSize, int writeQuorumSize, int ackQuorumSize, Map<String, byte[]> customMetadata, Set<BookieId> excludeBookies, Object expected){
        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.customMetadata = customMetadata;
        this.excludeBookies = excludeBookies;
        this.expected = expected;
    }
     */
    /*//Nuovo costruttore evo 1che supporta isWeighted
    public DefaultEnsemblePlacementPolicyTest(int ensembleSize, int writeQuorumSize, int ackQuorumSize, Map<String, byte[]> customMetadata, Set<BookieId> excludeBookies, boolean isWeighted, Object expected){
        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.customMetadata = customMetadata;
        this.excludeBookies = excludeBookies;
        this.isWeighted = isWeighted;
        this.expected = expected;
    }*/

    //Nuovo costruttore evo 2 che supporta weightedRandomSelection e knownBookies
    public DefaultEnsemblePlacementPolicyTest(int ensembleSize, int writeQuorumSize, int ackQuorumSize, Map<String, byte[]> customMetadata, Set<BookieId> excludeBookies, boolean isWeighted, Set<BookieId> knownBookies, BookieType bt, Object expected){
        this.ensembleSize = ensembleSize;
        this.writeQuorumSize = writeQuorumSize;
        this.ackQuorumSize = ackQuorumSize;
        this.customMetadata = customMetadata;
        this.excludeBookies = excludeBookies;
        this.isWeighted = isWeighted;
        this.knownBookies = knownBookies;
        if(bt == BookieType.NULL)
            this.wrsb = null;
        else
            this.wrsb = Mockito.mock(WeightedRandomSelectionImpl.class);
        if(bt == BookieType.EMPTY)
            Mockito.when(wrsb.getNextRandom()).thenReturn(null);
        else if(bt == BookieType.NON_EMPTY_KNOWN)
            Mockito.when(wrsb.getNextRandom()).thenReturn(createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN).iterator().next());
        else if(bt == BookieType.NON_EMPTY_NON_KNOWN)
            Mockito.when(wrsb.getNextRandom()).thenReturn(createDummyHashSet(1, BookieType.NON_EMPTY_NON_KNOWN).iterator().next());
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
            weightedBookiesAttribute.set(esp, wrsb); //Necessario per non avere nullpointerexception
            //set knownBookies attribute with reflection
            knownBookiesAttribute.set(esp, knownBookies);

            ensembleBookies = esp.newEnsemble(ensembleSize, writeQuorumSize, ackQuorumSize, customMetadata, excludeBookies);
            System.out.println("Created " + ensembleBookies.getResult().size() +" bookies.");
            if(ensembleBookies.getResult().isEmpty())
                Assert.assertEquals(expected, ensembleBookies.getResult().size());
            else
                Assert.assertEquals(createDummyHashSet(1, BookieType.NON_EMPTY_KNOWN), new HashSet<>(ensembleBookies.getResult()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertEquals(expected.getClass(), e.getClass());
        }
    }
}
