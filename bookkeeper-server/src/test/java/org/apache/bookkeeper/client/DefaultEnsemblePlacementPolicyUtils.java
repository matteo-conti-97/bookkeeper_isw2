package org.apache.bookkeeper.client;

import org.apache.bookkeeper.net.BookieId;

import java.util.HashSet;
import java.util.Set;

public class DefaultEnsemblePlacementPolicyUtils {
    public enum BookieType {
        NULL,
        EMPTY,
        NON_EMPTY_KNOWN,
        NON_EMPTY_NON_KNOWN
    }
    private static final String WITH_EMPTY_MAP = "with empty map";
    private static final String WITH_NULL_MAP = "with null map";
    private static final String WITH_NON_EMPTY_MAP = "with non empty map";
    public static Set<BookieId> createDummyHashSet(int size, BookieType bt){
        Set<BookieId> dummySet = new HashSet<>();
        for(int i = 0; i < size; i++){
            //dummySet.add(Mockito.mock(BookieId.class));//Mockito non supporta il mocking di classi final
            if(bt==BookieType.NON_EMPTY_KNOWN)
                dummySet.add(BookieId.parse("testBookie"+i));
            else
                dummySet.add(BookieId.parse("testBookieNotKnown"+i));
        }
        return dummySet;
    }
}
