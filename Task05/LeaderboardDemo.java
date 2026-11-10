package Task05;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Team {
    final String name;
    final int points;
    final int goalDiff;

    Team(String name, int points, int goalDiff) {
        this.name = name;
        this.points = points;
        this.goalDiff = goalDiff;
    }

    public String getName() { return name; }
    public int getPoints() { return points; }
    public int getGoalDiff() { return goalDiff; }

    @Override
    public String toString() {
        return name + "(" + points + "," + goalDiff + ")";
    }
}

class Leaderboard<T> {
    private final List<T> items = new ArrayList<>();
    private final Comparator<? super T> order;

    Leaderboard(Comparator<? super T> order) {
        this.order = order;
    }

    void add(T item) {
        items.add(item);
    }

    List<T> ranking() {
        // TODO 1: Return a new sorted copy of items using order, without modifying items
        List<T> copy = new ArrayList<>(this.items);
        copy.sort(this.order);
        return copy;
    }
}

public class LeaderboardDemo {
    public static void main(String[] args) {
        // TODO 2: Build the comparator: points (high first), then goal difference (high first), then name (A-Z)
        Comparator<Team> byRank = Comparator
                .comparingInt(Team::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(Team::getGoalDiff).reversed())
                .thenComparing(Team::getName);

        Leaderboard<Team> board = new Leaderboard<>(byRank);
        board.add(new Team("Panthers", 9, 4));
        board.add(new Team("Eagles", 9, 6));
        board.add(new Team("Cobras", 6, 1));
        board.add(new Team("Bisons", 9, 4));

        System.out.println(board.ranking());
    }
}
