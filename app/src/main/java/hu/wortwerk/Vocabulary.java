package hu.wortwerk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Vocabulary {
    public static final class Word {
        public final String id, icon, german, hungarian, example, translation;
        Word(String id, String[] parts) {
            this.id = id; icon = parts[0]; german = parts[1]; hungarian = parts[2];
            example = parts[3]; translation = parts[4];
        }
    }
    public static final class Category {
        public final String id, icon, title, subtitle;
        public final List<Word> words = new ArrayList<>();
        Category(String[] parts) { id=parts[0]; icon=parts[1]; title=parts[2]; subtitle=parts[3]; }
    }
    public static List<Category> read(InputStream stream) throws IOException {
        List<Category> result = new ArrayList<>();
        Category current = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line=reader.readLine())!=null) {
                if (line.trim().isEmpty()) continue;
                String[] parts=line.split("\\|", -1);
                if(parts.length==4) { current=new Category(parts); result.add(current); }
                else if(parts.length==5 && current!=null) {
                    current.words.add(new Word(current.id+"-"+current.words.size(),parts));
                } else throw new IOException("Invalid vocabulary row");
            }
        }
        return result;
    }
}
