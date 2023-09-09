package org.apache.bookkeeper.client;

import org.apache.bookkeeper.net.BookieId;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

public class DefaultEnsemblePlacementPolicyUtils {
    public static final String NULL_SET = "null";
    public static final String EMPTY_SET = "empty";
    public static final String NON_EMPTY_SET = "nonEmpty";

    public static Set<BookieId> createDummyHashSet(int size){
        Set<BookieId> dummySet = new HashSet<>();
        for(int i = 0; i < size; i++){
            dummySet.add(Mockito.mock(BookieId.class));
            //dummySet.add(BookieId.parse("testBookie"+i));
        }
        return dummySet;
    }
}
