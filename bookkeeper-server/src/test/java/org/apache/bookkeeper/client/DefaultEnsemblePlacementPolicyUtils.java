package org.apache.bookkeeper.client;

import org.apache.bookkeeper.net.BookieId;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

public class DefaultEnsemblePlacementPolicyUtils {

    public enum wrsbType{
        NULL,
        EMPTY,
        IN_EXCLUDE,
        NOT_IN_EXCLUDE
    }
    public static Set<BookieId> createDummyHashSet(int size, boolean isKnown){
        Set<BookieId> dummySet = new HashSet<>();
        for(int i = 0; i < size; i++){
            //dummySet.add(Mockito.mock(BookieId.class));//Mockito non supporta il mocking di classi final
            if(isKnown)
                dummySet.add(BookieId.parse("testBookieKnown"+i));
            else
                dummySet.add(BookieId.parse("testBookieNotKnown"+i));
        }
        return dummySet;
    }
}
