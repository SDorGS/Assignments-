public class Board {
    private Pit[] pits;

    public Board() {
        pits = new Pit[12];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 12; i++) {
            pits[i] = new Pit();
        }
    }

    public Pit getPit(int index) {
        return pits[index];
    }

    public Board deepCopy() {
        Board copy = new Board();
        for (int i = 0; i < pits.length; i++) {
            copy.pits[i] = new Pit();
            for (int j = 0; j < this.pits[i].getSeedCount(); j++) {
                copy.pits[i].addSeed(new Seed());
            }
        }
        return copy;
    }
}
