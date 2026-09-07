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
            assertEquals(100,c.words.size());Set<String> german=new HashSet<>(),hungarian=new HashSet<>();
            for(Vocabulary.Word w:c.words) {
                assertTrue(ids.add(w.id));assertTrue(german.add(w.german));assertTrue(hungarian.add(w.hungarian));
                for(String field:Arrays.asList(w.icon,w.german,w.hungarian,w.example,w.translation))
                    assertFalse(field.trim().isEmpty());
            }
        }
        assertEquals(500,ids.size());
    }

    private List<Vocabulary.Category> allCategories() throws Exception {
        return Vocabulary.read(Files.newInputStream(Paths.get(
            System.getProperty("vocabulary.path","src/main/assets/vocabulary.txt"))));
    }
    private List<String> ids(Vocabulary.Category category) {
        List<String> result=new ArrayList<>();
        for(Vocabulary.Word word:category.words) result.add(word.id);
        return result;
    }
    @Test public void everyWordInEveryCategoryIsTestedOncePerCycle() throws Exception {
        for(Vocabulary.Category c:allCategories()) {
            List<String> all=ids(c);
            Set<String> completed=new HashSet<>();
            Random rng=new Random(123);
            for(int i=0;i<20;i++) {
                List<String> chosen=PracticeDeck.next(all,completed,rng);
                assertEquals(5,chosen.size());
                MatchingRound round=new MatchingRound(chosen,rng);
                for(String id:round.left) {
                    assertTrue("Wrong category: "+id,all.contains(id));
                    assertFalse("Repeated completed word: "+id,completed.contains(id));
                    assertTrue(round.choose(id));
                    assertTrue(round.answer(id));
                }
                assertTrue(round.complete());
                assertEquals(100,round.accuracy());
                completed.addAll(round.matched);
            }
            assertEquals(new HashSet<>(all),completed);
            assertTrue("Last added word must be testable",completed.contains(c.id+"-99"));
        }
    }
    @Test public void practiceResumesFromSavedCompletedWords() throws Exception {
        List<String> all=ids(allCategories().get(0));
        Set<String> saved=new HashSet<>(all.subList(0,95));
        Set<String> restored=new HashSet<>(saved);
        List<String> chosen=PracticeDeck.next(all,restored,new Random(2));
        assertEquals(new HashSet<>(all.subList(95,100)),new HashSet<>(chosen));
        assertEquals("Selection must not mark words completed",saved,restored);
    }
    @Test public void practiceFinishesShortRemainderBeforeRepeating() {
        List<String> all=Arrays.asList("a","b","c","d","e","f","g");
        Set<String> completed=new HashSet<>(all.subList(0,5));
        assertEquals(new HashSet<>(Arrays.asList("f","g")),
            new HashSet<>(PracticeDeck.next(all,completed,new Random(3))));
    }
    @Test public void completedCycleCanStartAgainWithoutDuplicatePairs() throws Exception {
        List<String> all=ids(allCategories().get(0));
        List<String> chosen=PracticeDeck.next(all,new HashSet<>(all),new Random(4));
        assertEquals(5,chosen.size());
        assertEquals(5,new HashSet<>(chosen).size());
        assertTrue(all.containsAll(chosen));
    }
    @Test public void originalSavedWordIdsStillHaveTheSameMeanings() throws Exception {
        Map<String,Vocabulary.Word> actual=new HashMap<>();
        for(Vocabulary.Category c:allCategories()) for(Vocabulary.Word w:c.words) actual.put(w.id,w);
        InputStream stream=getClass().getResourceAsStream("/legacy-word-ids.txt");
        assertNotNull("Legacy fixture is required",stream);
        int count=0;
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8))) {
            String row;
            while((row=reader.readLine())!=null) {
                String[] fields=row.split("\\|");
                Vocabulary.Word w=actual.get(fields[0]);
                assertNotNull(w);assertEquals(fields[1],w.german);assertEquals(fields[2],w.hungarian);
                count++;
            }
        }
        assertEquals(60,count);
    }

    @Test(expected=IOException.class) public void malformedVocabularyFailsClearly() throws Exception {
        Vocabulary.read(new ByteArrayInputStream("broken row".getBytes("UTF-8")));
    }
}
