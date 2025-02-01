public class Player {
    private String name;
    private int score;
    private boolean isAI;
    private int pitStart;
    private int pitEnd;

    public Player(String name, boolean isAI, int pitStart, int pitEnd) {
        this.name = name;
        this.isAI = isAI;
        this.pitStart = pitStart;
        this.pitEnd = pitEnd;
        this.score = 0;
    }

    public String getName() {
        return name;
    }
    
    public int getScore() {
        return score;
    }
    
    public void addScore(int points) {
        score += points;
    }
    
    public boolean isAI() {
        return isAI;
    }
    
    public int getPitStart() {
        return pitStart;
    }
    
    public int getPitEnd() {
        return pitEnd;
    }
}
