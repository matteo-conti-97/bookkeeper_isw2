package org.apache.bookkeeper.client;

import org.apache.bookkeeper.net.BookieId;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

public class DefaultEnsemblePlacementPolicyUtils {

    public static Set<BookieId> createDummyHashSet(int size){
        Set<BookieId> dummySet = new HashSet<>();
        for(int i = 0; i < size; i++){
            //dummySet.add(Mockito.mock(BookieId.class));//Mockito non supporta il mocking di classi final
            dummySet.add(BookieId.parse("testBookie"+i));
        }
        return dummySet;
    }
}
