package hu.wortwerk;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final int INK=Color.rgb(25,49,45), GREEN=Color.rgb(20,104,92),
        BG=Color.rgb(246,247,242), MUTED=Color.rgb(91,111,104);
    private List<Vocabulary.Category> categories;
    private SharedPreferences prefs;
    private Set<String> learned;
    private LinearLayout page, body;
    private String screen="home", categoryId="", feedback="";
    private MatchingRound round;
    private TextToSpeech speech;
    private boolean speechReady;
    private int wordIndex=0;
    private final Random random=new Random();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs=getSharedPreferences("learning",MODE_PRIVATE);
        learned=new HashSet<>(prefs.getStringSet("learned",Collections.emptySet()));
        try { categories=Vocabulary.read(getAssets().open("vocabulary.txt")); }
        catch(Exception e) { TextView error=new TextView(this); error.setText("A szókincs nem tölthető be."); setContentView(error); return; }
        if(state!=null) {
            screen=state.getString("screen","home"); categoryId=state.getString("category","");
            wordIndex=state.getInt("word",0); feedback=state.getString("feedback","");
            ArrayList<String> left=state.getStringArrayList("left");
            if(left!=null) {
                round=new MatchingRound(left,random);
                round.left.clear(); round.left.addAll(left);
                round.right.clear(); round.right.addAll(state.getStringArrayList("right"));
                round.matched.addAll(state.getStringArrayList("matched"));
                round.selected=state.getString("selected"); round.attempts=state.getInt("attempts");
            }
        }
        speech=new TextToSpeech(this,status -> {
            if(status==TextToSpeech.SUCCESS) {
                int result=speech.setLanguage(Locale.GERMAN);
                speechReady=result!=TextToSpeech.LANG_MISSING_DATA && result!=TextToSpeech.LANG_NOT_SUPPORTED;
                speech.setSpeechRate(0.85f);
            }
        });
        render();
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private GradientDrawable shape(int color) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(20)); return d;
    }
    private TextView text(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(0,dp(5),0,dp(5));
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private Button button(String label, boolean primary, Runnable action) {
        Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(16);
        b.setTextColor(primary?Color.WHITE:GREEN); b.setBackground(shape(primary?GREEN:Color.rgb(231,239,232)));
        b.setMinHeight(dp(52)); b.setPadding(dp(12),dp(10),dp(12),dp(10));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(6),0,dp(6));
        b.setLayoutParams(lp); b.setOnClickListener(v -> action.run()); return b;
    }
    private LinearLayout panel() {
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(20),dp(18),dp(20),dp(18)); p.setBackground(shape(Color.WHITE));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(8),0,dp(8));
        p.setLayoutParams(lp); body.addView(p); return p;
    }
    private Vocabulary.Category category() {
        for(Vocabulary.Category c:categories) if(c.id.equals(categoryId)) return c;
        return categories.get(0);
    }
    private Vocabulary.Word word(String id) {
        for(Vocabulary.Word w:category().words) if(w.id.equals(id))return w;
        throw new IllegalArgumentException(id);
    }
    private void render() {
        page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setBackgroundColor(BG);
        page.setOnApplyWindowInsetsListener((v,insets)-> {
            v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom()); return insets;
        });
        setContentView(page); page.requestApplyInsets();
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(22),dp(14),dp(22),dp(24)); scroll.addView(body);
        if(!screen.equals("home")) body.addView(button("‹  Kategóriák",false,()->{screen="home";render();}));
        if(screen.equals("home")) home();
        else if(screen.equals("learn")) learn();
        else practice();
    }
    private int totalWords() {
        int total=0;
        for(Vocabulary.Category c:categories) total+=c.words.size();
        return total;
    }
    private Set<String> practiced(Vocabulary.Category c) {
        Set<String> result=new HashSet<>(prefs.getStringSet("practiced-"+c.id,Collections.emptySet()));
        Set<String> valid=new HashSet<>();
        for(Vocabulary.Word w:c.words) valid.add(w.id);
        result.retainAll(valid);
        return result;
    }
    private void chooseWord() {
        Vocabulary.Category c=category();
        String[] labels=new String[c.words.size()];
        for(int i=0;i<labels.length;i++) {
            Vocabulary.Word w=c.words.get(i);
            labels[i]=(i+1)+". "+w.german+" — "+w.hungarian;
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Válassz szót · "+c.words.size())
            .setSingleChoiceItems(labels,wordIndex,(dialog,index)->{
                wordIndex=index;dialog.dismiss();render();
            })
            .setNegativeButton("Mégse",null).show();
    }
    private void home() {
        body.addView(text("WORTWERK  /  HU → DE",13,GREEN,true));
        body.addView(text("Egy szóval közelebb.",32,INK,true));
        body.addView(text("Német a munkához és a mindennapokhoz.",16,MUTED,false));
        LinearLayout summary=panel();
        summary.addView(text("🌱  "+learned.size()+" / "+totalWords()+" szó megtanulva",21,GREEN,true));
        summary.addView(text("Válassz témát. Tanulj szavakat. Párosíts!",15,MUTED,false));
        for(Vocabulary.Category c:categories) {
            LinearLayout card=panel();
            card.addView(text(c.icon+"  "+c.title,23,INK,true));
            card.addView(text(c.subtitle,15,MUTED,false));
            int count=0; for(Vocabulary.Word w:c.words)if(learned.contains(w.id))count++;
            card.addView(text(count+" / "+c.words.size()+" megtanulva"+(prefs.contains("best-"+c.id)?"  ·  Legjobb: "+prefs.getInt("best-"+c.id,0)+"%":""),14,GREEN,true));
            card.addView(text("Gyakorolt szavak: "+practiced(c).size()+" / "+c.words.size(),14,MUTED,false));
            ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
            progress.setMax(c.words.size());progress.setProgress(count);
            progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));card.addView(progress);
            card.addView(button("Szavak felfedezése →",true,()->{categoryId=c.id;wordIndex=0;screen="learn";render();}));
            card.addView(button("Párosító gyakorlat",false,()->{categoryId=c.id;startRound();}));
        }
        body.addView(text("Offline szókincs · Saját tempó · Napi néhány perc",13,MUTED,false));
    }
    private void speak(String value) {
        if(!speechReady) {
            Toast.makeText(this,"A német felolvasáshoz telepíts német beszédhangot a telefon szövegfelolvasási beállításaiban.",Toast.LENGTH_LONG).show();
            return;
        }
        if(speech.speak(value,TextToSpeech.QUEUE_FLUSH,null,"word")==TextToSpeech.ERROR)
            Toast.makeText(this,"A felolvasás most nem elérhető.",Toast.LENGTH_SHORT).show();
    }
    private void learn() {
        Vocabulary.Category c=category();
        Vocabulary.Word w=c.words.get(wordIndex);
        body.addView(text(c.icon+"  "+c.title,25,INK,true));
        body.addView(text("SZÓKÁRTYÁK  ·  "+(wordIndex+1)+" / "+c.words.size(),13,MUTED,true));
        body.addView(button("Szó választása · "+c.words.size()+" szó",false,()->chooseWord()));
        LinearLayout card=panel();
        TextView icon=text(w.icon,64,INK,false); icon.setGravity(Gravity.CENTER); icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO); card.addView(icon);
        card.addView(text(w.german,30,INK,true));
        card.addView(text(w.hungarian,23,GREEN,false));
        card.addView(button("🔊  Szó meghallgatása",false,()->speak(w.german)));
        card.addView(text("PÉLDAMONDAT",12,MUTED,true));
        card.addView(text(w.example,21,INK,false));
        card.addView(text(w.translation,17,MUTED,false));
        card.addView(button("🔊  Mondat meghallgatása",false,()->speak(w.example)));
        card.addView(button(learned.contains(w.id)?"✓  Megtanultam — visszavonás":"Megtanultam",true,()->{
            if(!learned.add(w.id))learned.remove(w.id);
            prefs.edit().putStringSet("learned",new HashSet<>(learned)).apply();render();
        }));
        Button previous=button("←  Előző szó",false,()->{wordIndex--;render();});
        previous.setEnabled(wordIndex>0);body.addView(previous);
        Button next=button("Következő szó  →",true,()->{wordIndex++;render();});
        next.setEnabled(wordIndex<c.words.size()-1);body.addView(next);
        body.addView(button("Gyakorlás ebben a témában",false,()->startRound()));
        body.addView(text("A der, die és das a főnevek névelője. Tanuld a szóval együtt!",14,MUTED,false));
    }
    private void startRound() {
        List<String> ids=new ArrayList<>();
        for(Vocabulary.Word w:category().words)ids.add(w.id);
        Set<String> completed=practiced(category());
        if(completed.containsAll(ids)) {
            completed.clear();
            prefs.edit().remove("practiced-"+categoryId).apply();
        }
        round=new MatchingRound(PracticeDeck.next(ids,completed,random),random);
        feedback="";screen="practice";render();
    }
    private void practice() {
        if(round==null) {startRound();return;}
        body.addView(text(category().icon+"  "+category().title,25,INK,true));
        body.addView(text("Találd meg a párját!",28,INK,true));
        body.addView(text("Válassz egy magyar szót, majd a német párját.",16,MUTED,false));
        body.addView(text(round.matched.size()+" / "+round.left.size()+" pár  ·  "+round.attempts+" próbálkozás",15,GREEN,true));
        if(round.complete()) {
            int score=round.accuracy();
            int best=Math.max(score,prefs.getInt("best-"+categoryId,0));
            Set<String> completed=practiced(category());
            completed.addAll(round.left);
            prefs.edit().putInt("best-"+categoryId,best)
                .putStringSet("practiced-"+categoryId,new HashSet<>(completed)).apply();
            LinearLayout result=panel();
            result.addView(text("🎉  Szép munka!",30,INK,true));
            result.addView(text(score+"% pontosság",26,GREEN,true));
            result.addView(text(round.left.size()+" pár, "+round.attempts+" próbálkozás.\nLegjobb eredmény ebben a témában: "+best+"%",17,MUTED,false));
            result.addView(text("Gyakorolt szavak: "+completed.size()+" / "+category().words.size(),18,GREEN,true));
            boolean fullCycle=completed.size()==category().words.size();
            if(fullCycle) result.addView(text("A kategória minden szavát gyakoroltad!",17,INK,true));
            result.addView(button(fullCycle?"Új sorozat indítása":"Következő 5 szó",true,()->startRound()));
            result.addView(button("Szavak ismétlése",false,()->{screen="learn";wordIndex=0;render();}));
            return;
        }
        body.addView(text("Ebben a sorozatban: "+practiced(category()).size()+" / "+category().words.size()+" szó kész",14,MUTED,false));
        TextView status=text(feedback.isEmpty()?"Kezdd egy magyar szóval.":feedback,16,INK,true);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);body.addView(status);
        LinearLayout columns=new LinearLayout(this); columns.setOrientation(LinearLayout.HORIZONTAL);
        body.addView(columns);
        LinearLayout left=new LinearLayout(this),right=new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);right.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,0,dp(6),0);columns.addView(left,lp);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,-2,1);rp.setMargins(dp(6),0,0,0);columns.addView(right,rp);
        left.addView(text("MAGYAR",12,MUTED,true));right.addView(text("DEUTSCH",12,MUTED,true));
        for(String id:round.left) {
            boolean matched=round.matched.contains(id);
            Button b=button((matched?"✓ ":id.equals(round.selected)?"• ":"")+word(id).hungarian,id.equals(round.selected),()->{
                round.choose(id);feedback="Kiválasztva: "+word(id).hungarian;render();
            });
            b.setMinHeight(dp(84));b.setEnabled(!matched);b.setAlpha(matched?0.45f:1f);left.addView(b);
        }
        for(String id:round.right) {
            boolean matched=round.matched.contains(id);
            Button b=button((matched?"✓ ":"")+word(id).german,false,()->{
                if(round.selected==null){feedback="Először válassz egy magyar szót.";render();return;}
                String selected=round.selected;
                boolean correct=round.answer(id);
                feedback=correct?"✓ Helyes: "+word(selected).hungarian+" = "+word(id).german:"Még nem jó. Válassz újra egy magyar szót!";
                render();
            });
            b.setMinHeight(dp(84));b.setEnabled(!matched);b.setAlpha(matched?0.45f:1f);right.addView(b);
        }
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("screen",screen);out.putString("category",categoryId);out.putInt("word",wordIndex);out.putString("feedback",feedback);
        if(round!=null) {
            out.putStringArrayList("left",new ArrayList<>(round.left));out.putStringArrayList("right",new ArrayList<>(round.right));
            out.putStringArrayList("matched",new ArrayList<>(round.matched));out.putString("selected",round.selected);out.putInt("attempts",round.attempts);
        }
    }
    @Override public void onBackPressed() {
        if(!screen.equals("home")){screen="home";render();}else super.onBackPressed();
    }
    @Override protected void onDestroy() { if(speech!=null){speech.stop();speech.shutdown();}super.onDestroy(); }
}
