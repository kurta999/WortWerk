package hu.wortwerk;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;

public class LearningTest {
    private MatchingRound round() { return new MatchingRound(Arrays.asList("a","b","c","d","e"),new Random(42)); }
    @Test public void noAttemptWithoutSelection() {
        MatchingRound r=round();assertFalse(r.answer("a"));assertEquals(0,r.attempts);
    }
    @Test public void wrongAnswerClearsSelectionAndCountsAttempt() {
        MatchingRound r=round();r.choose("a");assertFalse(r.answer("b"));
        assertNull(r.selected);assertEquals(1,r.attempts);assertTrue(r.matched.isEmpty());
    }
    @Test public void matchedWordsCannotScoreTwice() {
        MatchingRound r=round();r.choose("a");assertTrue(r.answer("a"));
        assertFalse(r.choose("a"));assertFalse(r.answer("a"));assertEquals(1,r.attempts);
        r.choose("b");assertFalse(r.answer("a"));assertEquals(1,r.attempts);
    }
    @Test public void completeRoundReportsAccuracy() {
        MatchingRound r=round();r.choose("a");r.answer("b");
        for(String id:r.left){r.choose(id);assertTrue(r.answer(id));}
        assertTrue(r.complete());assertEquals(83,r.accuracy());assertEquals(6,r.attempts);
    }
    @Test public void bothColumnsContainExactlyTheSameWords() {
        MatchingRound r=round();assertEquals(new HashSet<>(r.left),new HashSet<>(r.right));
        assertEquals(5,new HashSet<>(r.left).size());
    }
    @Test public void vocabularyIsCompleteAndUnambiguousWithinCategories() throws Exception {
        String location=System.getProperty("vocabulary.path","src/main/assets/vocabulary.txt");
        List<Vocabulary.Category> categories=Vocabulary.read(Files.newInputStream(Paths.get(location)));
        assertEquals(5,categories.size());Set<String> ids=new HashSet<>();
        for(Vocabulary.Category c:categories) {
            assertEquals(12,c.words.size());Set<String> german=new HashSet<>(),hungarian=new HashSet<>();
            for(Vocabulary.Word w:c.words) {
                assertTrue(ids.add(w.id));assertTrue(german.add(w.german));assertTrue(hungarian.add(w.hungarian));
                for(String field:Arrays.asList(w.icon,w.german,w.hungarian,w.example,w.translation))
                    assertFalse(field.trim().isEmpty());
            }
        }
        assertEquals(60,ids.size());
    }
    @Test(expected=IOException.class) public void malformedVocabularyFailsClearly() throws Exception {
        Vocabulary.read(new ByteArrayInputStream("broken row".getBytes("UTF-8")));
    }
}
