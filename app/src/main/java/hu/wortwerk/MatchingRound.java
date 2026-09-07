package hu.wortwerk;

import java.util.*;

public final class MatchingRound {
    public final List<String> left, right;
    public final Set<String> matched = new HashSet<>();
    public String selected;
    public int attempts;
    public MatchingRound(List<String> ids, Random random) {
        left=new ArrayList<>(ids); right=new ArrayList<>(ids);
        Collections.shuffle(left,random); Collections.shuffle(right,random);
    }
    public boolean choose(String id) {
        if(!left.contains(id) || matched.contains(id)) return false;
        selected=id; return true;
    }
    public boolean answer(String id) {
        if(selected==null || !right.contains(id) || matched.contains(id)) return false;
        attempts++;
        boolean correct=selected.equals(id);
        if(correct) matched.add(id);
        selected=null;
        return correct;
    }
    public boolean complete() { return matched.size()==left.size(); }
    public int accuracy() { return attempts==0?0:Math.round(100f*matched.size()/attempts); }
}
