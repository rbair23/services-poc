package com.hedera.hashgraph.hapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Static utility methods used in generated tests
 */
public final class HapiTestUtils {
    /**
     * Take a list of objects and create a new list with those objects wrapped in optionals and adding a empty optional.
     *
     * @param list List of objects to wrap
     * @return list of optionals
     * @param <T> type of objects to wrap
     */
    public static <T> List<Optional<T>> makeListOptionals(List<T> list) {
        ArrayList<Optional<T>> optionals = new ArrayList<>(list.size()+1);
        optionals.add(Optional.empty());
        for (T value:list) {
            optionals.add(Optional.ofNullable(value));
        }
        return optionals;
    }


    /**
     * Util method to create a list of lists of objects. Given a list of test cases it creates a empty list and then a
     * list for each subset of input list till it includes the whole list.
     *
     * @param list Input list
     * @return list of lists derived from input list
     * @param <T> the type for lists
     */
    public static <T> List<List<T>> generateListArguments(final List<T> list) {
        return IntStream.range(0,list.size())
                .mapToObj(i -> list.subList(0,i))
                .toList();
    }
}
