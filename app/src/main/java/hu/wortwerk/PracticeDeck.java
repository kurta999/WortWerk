package hu.wortwerk;

import java.util.*;

/** Selects unseen words within one category. Completed cycles may start again. */
public final class PracticeDeck {
    private PracticeDeck() {}
    public static List<String> next(List<String> categoryIds, Set<String> completed, Random random) {
        List<String> remaining = new ArrayList<>();
        for (String id : categoryIds) if (!completed.contains(id)) remaining.add(id);
        if (remaining.isEmpty()) remaining.addAll(categoryIds);
        Collections.shuffle(remaining, random);
        return new ArrayList<>(remaining.subList(0, Math.min(5, remaining.size())));
    }
}
