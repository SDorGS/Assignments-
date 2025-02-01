import java.util.Random;
import java.util.Scanner;
import java.util.List;

public class AyoGame {
    private Board board;
    private Player playerA;
    private Player playerB;
    private Player currentPlayer;
    private Scanner scanner = new Scanner(System.in);
    private Random random = new Random();

    public AyoGame(boolean singlePlayer) {
        playerA = new Player("Player A", false, 0, 5);
        playerB = new Player("Player B", singlePlayer, 6, 11);
        currentPlayer = playerA;
        board = new Board();
    }

    public void startGame() {
        System.out.println("Welcome to Ayo (Oware)!");
        displayBoard();

        while (!isGameOver()) {
            playTurn();
        }

        finalizeGame();
    }

    private void playTurn() {
        System.out.println("\n" + currentPlayer.getName() + "'s turn.");
        if (currentPlayer.isAI()) {
            aiMove();
        } else {
            playerMove();
        }
        displayBoard();
        currentPlayer = (currentPlayer == playerA) ? playerB : playerA;
    }

    private void playerMove() {
        int chosenPit;
        do {
            System.out.print("Choose a pit (1-6): ");
            chosenPit = scanner.nextInt() - 1;
            if (!isValidMove(chosenPit)) {
                System.out.println("Invalid move! Choose a pit on your side that has seeds.");
            }
        } while (!isValidMove(chosenPit));

        executeMove(chosenPit);
    }

    private void aiMove() {
        System.out.println("AI is thinking...");
        int bestMove = findBestMove();
        System.out.println("AI chooses pit " + (bestMove + 1));
        executeMove(bestMove);
    }

    private int findBestMove() {
        int bestMove = -1;
        int maxSeedsCaptured = -1;

        for (int pit = 0; pit < 6; pit++) {
            if (!isValidMove(pit)) continue;

            int simulatedCapture = simulateChainMoveAndCapture(pit);

            if (simulatedCapture > maxSeedsCaptured) {
                maxSeedsCaptured = simulatedCapture;
                bestMove = pit;
            }
        }
        if (bestMove == -1) {
            do {
                bestMove = random.nextInt(6);
            } while (!isValidMove(bestMove));
        }
        return bestMove;
    }

    private int simulateChainMoveAndCapture(int pit) {
        Board boardClone = board.deepCopy();
        int simulatedCaptured = simulateExecuteMove(boardClone, pit);
        return simulatedCaptured;
    }

    private int simulateExecuteMove(Board boardClone, int pit) {
        int capturedScore = 0;
        int actualPit = currentPlayer.getPitStart() + pit;
        List<Seed> seeds = boardClone.getPit(actualPit).takeAllSeeds();
        int index = actualPit;
        
        while (true) {
            while (!seeds.isEmpty()) {
                index = (index + 1) % 12;
                Pit targetPit = boardClone.getPit(index);
                
                boolean wasEmpty = (targetPit.getSeedCount() == 0);
                boolean isLastSeed = (seeds.size() == 1);
                
                targetPit.addSeed(seeds.remove(0));
                
                if (isLastSeed && wasEmpty) {
                    capturedScore += simulateCaptureOnClone(boardClone, index);
                    return capturedScore;
                }
            }
            seeds = boardClone.getPit(index).takeAllSeeds();
            if (seeds.isEmpty()) {
                capturedScore += simulateCaptureOnClone(boardClone, index);
                return capturedScore;
            }
        }
    }

    private int simulateCaptureOnClone(Board boardClone, int lastPit) {
        int captured = 0;
        Player opponent = (currentPlayer == playerA) ? playerB : playerA;
        int opponentStart = opponent.getPitStart();
        int opponentEnd = opponent.getPitEnd();
        
        if (lastPit < opponentStart || lastPit > opponentEnd) {
            return 0;
        }
        
        int count = boardClone.getPit(lastPit).getSeedCount();
        if (count != 1 && count != 2) {
            return 0;
        }
        
        int pitIndex = lastPit;
        while (pitIndex >= opponentStart &&
               (boardClone.getPit(pitIndex).getSeedCount() == 1 ||
                boardClone.getPit(pitIndex).getSeedCount() == 2)) {
            captured += boardClone.getPit(pitIndex).getSeedCount();
            boardClone.getPit(pitIndex).clearSeeds();
            pitIndex--;
        }
        return captured;
    }

    private boolean isValidMove(int pit) {
        if (pit < 0 || pit >= 6) return false;
        int actualPit = currentPlayer.getPitStart() + pit;
        return board.getPit(actualPit).getSeedCount() > 0;
    }

    private void executeMove(int pit) {
        int actualPit = currentPlayer.getPitStart() + pit;
        List<Seed> seeds = board.getPit(actualPit).takeAllSeeds();
        int index = actualPit;

        while (!seeds.isEmpty()) {
            index = (index + 1) % 12;
            Pit targetPit = board.getPit(index);

            targetPit.addSeed(seeds.remove(0));

            if (seeds.isEmpty()) {
                captureSeeds(index);
            }
        }
    }

    private void captureSeeds(int lastPit) {
        Player opponent = (currentPlayer == playerA) ? playerB : playerA;
        int opponentStart = opponent.getPitStart();
        int opponentEnd = opponent.getPitEnd();

        if (lastPit < opponentStart || lastPit > opponentEnd) {
            return;
        }

        int count = board.getPit(lastPit).getSeedCount();
        if (count != 1 && count != 2) {
            return;
        }

        int captured = 0;
        int pitIndex = lastPit;
        while (pitIndex >= opponentStart &&
               (board.getPit(pitIndex).getSeedCount() == 1 || board.getPit(pitIndex).getSeedCount() == 2)) {
            captured += board.getPit(pitIndex).getSeedCount();
            board.getPit(pitIndex).clearSeeds();
            pitIndex--;
        }

        if (captured > 0) {
            currentPlayer.addScore(captured);
        }
    }

    private boolean isGameOver() {
        return isSideEmpty(currentPlayer.getPitStart(), currentPlayer.getPitEnd());
    }

    private boolean isSideEmpty(int start, int end) {
        for (int i = start; i <= end; i++) {
            if (board.getPit(i).getSeedCount() > 0) {
                return false;
            }
        }
        return true;
    }

    private void finalizeGame() {
        System.out.println("\nGame Over!");
        System.out.println("Final Scores:");
        System.out.println(playerA.getName() + ": " + playerA.getScore());
        System.out.println(playerB.getName() + ": " + playerB.getScore());

        if (playerA.getScore() > playerB.getScore()) {
            System.out.println("🎉 " + playerA.getName() + " Wins! 🎉");
        } else if (playerB.getScore() > playerA.getScore()) {
            System.out.println("🎉 " + playerB.getName() + " Wins! 🎉");
        } else {
            System.out.println("It's a Draw!");
        }
    }

    private void displayBoard() {
        System.out.println("\n" + playerB.getName() + " Score: " + playerB.getScore());
        System.out.println("---------------------------------");

        System.out.print("| ");
        for (int i = playerB.getPitEnd(); i >= playerB.getPitStart(); i--) {
            System.out.printf("%2d  | ", board.getPit(i).getSeedCount());
        }
        System.out.println(" (" + playerB.getName() + ")");

        System.out.println("---------------------------------");

        System.out.print("| ");
        for (int i = playerA.getPitStart(); i <= playerA.getPitEnd(); i++) {
            System.out.printf("%2d  | ", board.getPit(i).getSeedCount());
        }
        System.out.println(" (" + playerA.getName() + ")");

        System.out.println("---------------------------------");
        System.out.println(playerA.getName() + " Score: " + playerA.getScore());
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Single-player mode? (yes/no): ");
        boolean singlePlayer = scanner.next().equalsIgnoreCase("yes");

        AyoGame game = new AyoGame(singlePlayer);
        game.startGame();
    }
}
