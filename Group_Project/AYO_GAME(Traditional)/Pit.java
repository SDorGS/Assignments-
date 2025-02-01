import java.util.ArrayList;
import java.util.List;

public class Pit {
    private List<Seed> seeds;

    public Pit() {
        this.seeds = new ArrayList<>();
        initializeSeeds();
    }

    private void initializeSeeds() {
        for (int i = 0; i < 4; i++) {
            seeds.add(new Seed());
        }
    }

    public int getSeedCount() {
        return seeds.size();
    }

    public void addSeed(Seed seed) {
        seeds.add(seed);
    }

    public List<Seed> takeAllSeeds() {
        List<Seed> takenSeeds = new ArrayList<>(seeds);
        seeds.clear();
        return takenSeeds;
    }
    public void clearSeeds() {
        seeds.clear();
    }

    @Override
    public String toString() {
        return "Pit{" + "seeds=" + getSeedCount() + '}';
    }
}
