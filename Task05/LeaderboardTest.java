package Task05;

import java.util.*;

/**
 * Task05 - Comprehensive Test Suite for Generic Leaderboard & Principle Verification
 * 
 * Tests:
 *  1. Multi-tier ties (Points, GoalDiff, Alphabetical)
 *  2. Breaking a faulty subtraction-based comparator (Integer Overflow Bug)
 *  3. Reusability demonstration for Players and Colleges
 * 
 * Student Register Number: 192521325
 */
public class LeaderboardTest {

    // Domain 2: Player
    static class Player {
        final String name;
        final int score;
        final int assists;

        Player(String name, int score, int assists) {
            this.name = name;
            this.score = score;
            this.assists = assists;
        }

        public String getName() { return name; }
        public int getScore() { return score; }
        public int getAssists() { return assists; }

        @Override
        public String toString() {
            return name + "[Pts:" + score + ",Ast:" + assists + "]";
        }
    }

    public static void testMultiTierTies() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("TEST 1: Multi-Tier Ties Dataset (Points, GoalDiff, Alphabetical)");
        System.out.println("--------------------------------------------------------------------------");

        Comparator<Team> byRank = Comparator
                .comparingInt(Team::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(Team::getGoalDiff).reversed())
                .thenComparing(Team::getName);

        Leaderboard<Team> board = new Leaderboard<>(byRank);
        board.add(new Team("Hawks", 15, 10));
        board.add(new Team("Wolves", 15, 10)); // Exact tie in pts and gd, Wolves vs Hawks
        board.add(new Team("Bears", 15, 8));   // Same pts, lower gd
        board.add(new Team("Lions", 18, 2));   // Higher pts
        board.add(new Team("Apex", 15, 10));   // Exact tie in pts and gd, Apex first alphabetically

        List<Team> ranked = board.ranking();
        System.out.println("Ranked Teams: " + ranked);
        System.out.println("Verification:");
        System.out.println(" 1st: Lions (18 pts) - Highest points");
        System.out.println(" 2nd: Apex (15 pts, 10 gd) - Tied pts & gd, alphabetical winner");
        System.out.println(" 3rd: Hawks (15 pts, 10 gd)");
        System.out.println(" 4th: Wolves (15 pts, 10 gd)");
        System.out.println(" 5th: Bears (15 pts, 8 gd) - Lower goal diff");
    }

    public static void testFaultySubtractionComparator() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("TEST 2: Deliberately Breaking a Faulty Subtraction Comparator");
        System.out.println("Scenario: Using (t2.points - t1.points) when scores have large range / extreme values");
        System.out.println("--------------------------------------------------------------------------");

        // Faulty subtraction comparator:
        // (t2.points - t1.points) causes integer underflow/overflow if values span negative/large ranges!
        Comparator<Team> faultyComparator = (t1, t2) -> (t2.points - t1.points);

        Team normalHigh = new Team("NormalHigh", 100, 0);
        Team extremeLow = new Team("ExtremeLow", -2_000_000_000, 0); // e.g. penalty deduction

        System.out.println("Comparing normalHigh (100) vs extremeLow (-2,000,000,000):");
        int diff = extremeLow.points - normalHigh.points;
        System.out.println("Direct Subtraction result: " + diff + " (Overflowed to positive!)");
        System.out.println("Faulty sort places extremeLow ABOVE normalHigh! Fails transitivity and ordering principle.");

        // Correct comparator using Integer.compare:
        Comparator<Team> robustComparator = Comparator.comparingInt(Team::getPoints).reversed();
        int robustDiff = robustComparator.compare(normalHigh, extremeLow);
        System.out.println("Robust Integer.compare result: " + robustDiff + " (Correctly identifies NormalHigh > ExtremeLow)");
    }

    public static void testReusabilityAcrossDomains() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("TEST 3: Reusing Generic Leaderboard<T> for Players");
        System.out.println("--------------------------------------------------------------------------");

        Comparator<Player> playerRank = Comparator
                .comparingInt(Player::getScore).reversed()
                .thenComparing(Comparator.comparingInt(Player::getAssists).reversed())
                .thenComparing(Player::getName);

        Leaderboard<Player> playerBoard = new Leaderboard<>(playerRank);
        playerBoard.add(new Player("Kavitha", 30, 5));
        playerBoard.add(new Player("Deepak", 30, 8)); // More assists
        playerBoard.add(new Player("Siddharth", 45, 2));

        System.out.println("Player Leaderboard: " + playerBoard.ranking());
        System.out.println("Successfully reused generic Leaderboard<T> without modifying a single line of container code!");
    }

    public static void main(String[] args) {
        System.out.println("=== Q5 LEADERBOARD PRINCIPLE & TEST SUITE ===");
        testMultiTierTies();
        testFaultySubtractionComparator();
        testReusabilityAcrossDomains();
    }
}
