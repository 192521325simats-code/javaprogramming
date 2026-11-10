package Task02;

import java.util.*;

/**
 * Task02 - When Is ArrayList an Appropriate Engineering Choice?
 * Music Playlist Editor & "Up Next" Queue Benchmark
 * 
 * Compares ArrayList, LinkedList, and ArrayDeque across:
 *  - Add at end
 *  - Insert at front / middle
 *  - Remove at front / middle
 *  - Access k-th element
 *  - Sequential iteration
 *  - Double-ended queue operations (Up Next Queue)
 * 
 * Student Register Number: 192521325
 */
public class PlaylistBenchmark {

    static class Song {
        final String title;
        final String artist;
        final int durationSeconds;

        Song(String title, String artist, int durationSeconds) {
            this.title = title;
            this.artist = artist;
            this.durationSeconds = durationSeconds;
        }
    }

    public static Song createSong(int id) {
        return new Song("Track_" + id, "Artist_" + (id % 50), 180 + (id % 120));
    }

    public static void benchmarkListOperations(int size) {
        System.out.println("==========================================================================");
        System.out.printf("PLAYLIST BENCHMARK: Size N = %,d songs%n", size);
        System.out.println("==========================================================================");

        int sampleOperations = Math.min(size, 5000); // number of test mutations

        // 1. Add at End (N elements)
        long start = System.nanoTime();
        List<Song> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) arrayList.add(createSong(i));
        double alAddEnd = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        List<Song> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) linkedList.add(createSong(i));
        double llAddEnd = (System.nanoTime() - start) / 1_000_000.0;

        // 2. Access k-th element (Random Get sampleOperations times)
        Random rand = new Random(42);
        int[] indices = new int[sampleOperations];
        for (int i = 0; i < sampleOperations; i++) indices[i] = rand.nextInt(size);

        start = System.nanoTime();
        long alDummy = 0;
        for (int idx : indices) alDummy += arrayList.get(idx).durationSeconds;
        double alGetTime = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        long llDummy = 0;
        // For large sizes, LinkedList.get(k) is O(n), so limit sample size for LinkedList to avoid hanging
        int llSamples = Math.min(sampleOperations, 1000);
        for (int i = 0; i < llSamples; i++) llDummy += linkedList.get(indices[i]).durationSeconds;
        double llGetTime = ((System.nanoTime() - start) / 1_000_000.0) * (sampleOperations / (double) llSamples);

        // 3. Iteration (Sequential pass through all N elements)
        start = System.nanoTime();
        long sumDurAl = 0;
        for (Song s : arrayList) sumDurAl += s.durationSeconds;
        double alIterTime = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        long sumDurLl = 0;
        for (Song s : linkedList) sumDurLl += s.durationSeconds;
        double llIterTime = (System.nanoTime() - start) / 1_000_000.0;

        // 4. Insert at Front (sampleOperations insertions at index 0)
        List<Song> alFrontCopy = new ArrayList<>(arrayList);
        start = System.nanoTime();
        for (int i = 0; i < sampleOperations; i++) alFrontCopy.add(0, createSong(99999 + i));
        double alInsertFront = (System.nanoTime() - start) / 1_000_000.0;

        List<Song> llFrontCopy = new LinkedList<>(linkedList);
        start = System.nanoTime();
        for (int i = 0; i < sampleOperations; i++) llFrontCopy.add(0, createSong(99999 + i));
        double llInsertFront = (System.nanoTime() - start) / 1_000_000.0;

        // 5. Insert in Middle (sampleOperations insertions at index size/2)
        List<Song> alMidCopy = new ArrayList<>(arrayList);
        start = System.nanoTime();
        for (int i = 0; i < sampleOperations; i++) alMidCopy.add(alMidCopy.size() / 2, createSong(99999 + i));
        double alInsertMid = (System.nanoTime() - start) / 1_000_000.0;

        List<Song> llMidCopy = new LinkedList<>(linkedList);
        start = System.nanoTime();
        int midSamples = Math.min(sampleOperations, 1000);
        for (int i = 0; i < midSamples; i++) llMidCopy.add(llMidCopy.size() / 2, createSong(99999 + i));
        double llInsertMid = ((System.nanoTime() - start) / 1_000_000.0) * (sampleOperations / (double) midSamples);

        // 6. Remove from Front
        start = System.nanoTime();
        for (int i = 0; i < sampleOperations; i++) alFrontCopy.remove(0);
        double alRemoveFront = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        for (int i = 0; i < sampleOperations; i++) llFrontCopy.remove(0);
        double llRemoveFront = (System.nanoTime() - start) / 1_000_000.0;

        System.out.printf("%-26s | %-16s | %-16s | %-12s%n", "Operation", "ArrayList (ms)", "LinkedList (ms)", "Winner");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-26s | %16.2f | %16.2f | %-12s%n", "Add at End (N items)", alAddEnd, llAddEnd, alAddEnd < llAddEnd ? "ArrayList" : "LinkedList");
        System.out.printf("%-26s | %16.2f | %16.2f | %-12s%n", "Get(k) (" + sampleOperations + " random)", alGetTime, llGetTime, "ArrayList (O(1))");
        System.out.printf("%-26s | %16.2f | %16.2f | %-12s%n", "Iterate (N items)", alIterTime, llIterTime, alIterTime < llIterTime ? "ArrayList" : "LinkedList");
        System.out.printf("%-26s | %16.2f | %16.2f | %-12s%n", "Insert Front (" + sampleOperations + " items)", alInsertFront, llInsertFront, alInsertFront < llInsertFront ? "ArrayList" : "LinkedList (O(1))");
        System.out.printf("%-26s | %16.2f | %16.2f | %-12s%n", "Insert Middle (" + sampleOperations + " items)", alInsertMid, llInsertMid, alInsertMid < llInsertMid ? "ArrayList" : "LinkedList");
        System.out.printf("%-26s | %16.2f | %16.2f | %-12s%n", "Remove Front (" + sampleOperations + " items)", alRemoveFront, llRemoveFront, alRemoveFront < llRemoveFront ? "ArrayList" : "LinkedList (O(1))");
        System.out.println();
    }

    public static void benchmarkUpNextQueue(int queueSize) {
        System.out.println("==========================================================================");
        System.out.printf("CHANGED CONDITION BENCHMARK: 'Up Next' Queue (%,d ops)%n", queueSize);
        System.out.println("Frequent addFirst, addLast, and removeFirst");
        System.out.println("==========================================================================");

        // 1. ArrayDeque
        ArrayDeque<Song> deque = new ArrayDeque<>();
        long start = System.nanoTime();
        for (int i = 0; i < queueSize; i++) {
            if (i % 2 == 0) deque.addFirst(createSong(i));
            else deque.addLast(createSong(i));
            if (i % 3 == 0 && !deque.isEmpty()) deque.pollFirst();
        }
        double dequeTime = (System.nanoTime() - start) / 1_000_000.0;

        // 2. LinkedList as Deque
        LinkedList<Song> llDeque = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < queueSize; i++) {
            if (i % 2 == 0) llDeque.addFirst(createSong(i));
            else llDeque.addLast(createSong(i));
            if (i % 3 == 0 && !llDeque.isEmpty()) llDeque.pollFirst();
        }
        double llTime = (System.nanoTime() - start) / 1_000_000.0;

        // 3. ArrayList (simulating front add/remove with index 0)
        int alLimit = Math.min(queueSize, 20000);
        ArrayList<Song> alQueue = new ArrayList<>();
        start = System.nanoTime();
        for (int i = 0; i < alLimit; i++) {
            if (i % 2 == 0) alQueue.add(0, createSong(i));
            else alQueue.add(createSong(i));
            if (i % 3 == 0 && !alQueue.isEmpty()) alQueue.remove(0);
        }
        double alTime = ((System.nanoTime() - start) / 1_000_000.0) * (queueSize / (double) alLimit);

        System.out.printf("%-18s | %-16s | %-20s%n", "Collection", "Time (ms)", "Behavior");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-18s | %16.2f | %-20s%n", "ArrayDeque", dequeTime, "Circular Array, O(1) ends, Cache-friendly");
        System.out.printf("%-18s | %16.2f | %-20s%n", "LinkedList", llTime, "Node allocations, pointer chasing");
        System.out.printf("%-18s | %16.2f | %-20s%n", "ArrayList", alTime, "Repeated System.arraycopy O(N)");
        System.out.printf("ArrayDeque Speedup vs LinkedList: %.2fx%n", llTime / dequeTime);
        System.out.printf("ArrayDeque Speedup vs ArrayList:  %.2fx%n", alTime / dequeTime);
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Q2 BENCHMARK: ARRAYLIST vs LINKEDLIST vs ARRAYDEQUE ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println();

        // 3 Playlist sizes: 1,000 (Personal playlist), 10,000 (Festival playlist), 100,000 (Master library catalogue)
        benchmarkListOperations(1000);
        benchmarkListOperations(10000);
        benchmarkListOperations(100000);

        // Changed Condition: "Up Next" Queue (50,000 operations)
        benchmarkUpNextQueue(50000);
    }
}
