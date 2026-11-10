package Task07;

import java.util.*;

/**
 * Task07 - Day-4 Design Challenge: Diagnose, Redesign and Validate
 * 
 * Compares:
 *  1. Original Solution: Java PriorityQueue<Patient> (O(log N) Binary Min-Heap)
 *  2. Redesigned Solution: MultiLevelBucketTriage (O(1) Array of 5 Circular ArrayDeques with O(1) Aging)
 * 
 * Student Register Number: 192521325
 */
public class TriageBenchmark {

    static class Patient {
        final String name;
        final int initialSeverity;
        int currentSeverity;
        final long arrivalTime;
        long treatedTime;

        Patient(String name, int severity, long arrivalTime) {
            this.name = name;
            this.initialSeverity = severity;
            this.currentSeverity = severity;
            this.arrivalTime = arrivalTime;
        }

        @Override
        public String toString() {
            return name + "(S" + currentSeverity + ")";
        }
    }

    // =========================================================================
    // 1. ORIGINAL SOLUTION: PriorityQueue (Binary Min-Heap)
    // =========================================================================
    static class OriginalPriorityQueueTriage {
        private final PriorityQueue<Patient> heap;
        private long counter = 0;

        public OriginalPriorityQueueTriage() {
            this.heap = new PriorityQueue<>(
                    Comparator.comparingInt((Patient p) -> p.currentSeverity)
                              .thenComparingLong(p -> p.arrivalTime)
            );
        }

        public void admit(String name, int severity) {
            heap.add(new Patient(name, severity, counter++)); // O(log N)
        }

        public Patient next() {
            return heap.poll(); // O(log N)
        }

        public int size() {
            return heap.size();
        }

        public boolean isEmpty() {
            return heap.isEmpty();
        }
    }

    // =========================================================================
    // 2. REDESIGNED SOLUTION: Multi-Level Priority Bucket Queue with O(1) Operations
    // =========================================================================
    static class RedesignedMultiLevelBucketTriage {
        private static final int NUM_LEVELS = 5;
        @SuppressWarnings("unchecked")
        private final ArrayDeque<Patient>[] buckets = new ArrayDeque[NUM_LEVELS];
        private int totalPatients = 0;
        private long counter = 0;

        public RedesignedMultiLevelBucketTriage() {
            for (int i = 0; i < NUM_LEVELS; i++) {
                buckets[i] = new ArrayDeque<>();
            }
        }

        // O(1) Enqueue: Direct array index mapping + tail addition in circular buffer
        public void admit(String name, int severity) {
            int level = Math.max(1, Math.min(NUM_LEVELS, severity));
            buckets[level - 1].addLast(new Patient(name, level, counter++));
            totalPatients++;
        }

        // O(1) Dequeue: Scans at most 5 level heads, dequeues from highest non-empty level
        public Patient next() {
            for (int i = 0; i < NUM_LEVELS; i++) {
                if (!buckets[i].isEmpty()) {
                    totalPatients--;
                    return buckets[i].pollFirst();
                }
            }
            return null;
        }

        // O(1) Anti-Starvation Aging: Promotes overdue head patients without full heap re-heapification
        public void applyAging(long currentTick, long waitThreshold) {
            // Check heads of lower-priority buckets (levels 2 to 5)
            for (int lvl = 1; lvl < NUM_LEVELS; lvl++) {
                while (!buckets[lvl].isEmpty()) {
                    Patient head = buckets[lvl].peekFirst();
                    if (currentTick - head.arrivalTime >= waitThreshold) {
                        // Dequeue from current level and promote to higher level in O(1)
                        Patient overdue = buckets[lvl].pollFirst();
                        overdue.currentSeverity--;
                        buckets[lvl - 1].addLast(overdue);
                    } else {
                        // Because bucket is in strict arrival order, if head has not exceeded
                        // threshold, no subsequent patient in this bucket has either! Break early!
                        break;
                    }
                }
            }
        }

        public int size() {
            return totalPatients;
        }

        public boolean isEmpty() {
            return totalPatients == 0;
        }
    }

    // =========================================================================
    // BENCHMARK HARNESS
    // =========================================================================
    public static void runThroughputBenchmark(int operations) {
        System.out.println("==========================================================================");
        System.out.printf("HEAD-TO-HEAD BENCHMARK: %,d Total Admissions and Discharges%n", operations);
        System.out.println("==========================================================================");

        Random rand = new Random(42);
        int[] severities = new int[operations];
        for (int i = 0; i < operations; i++) {
            severities[i] = 1 + rand.nextInt(5);
        }

        // Warmup
        OriginalPriorityQueueTriage warmupOrig = new OriginalPriorityQueueTriage();
        RedesignedMultiLevelBucketTriage warmupRedesign = new RedesignedMultiLevelBucketTriage();
        for (int i = 0; i < 5000; i++) {
            warmupOrig.admit("W" + i, severities[i]);
            warmupRedesign.admit("W" + i, severities[i]);
        }
        while (!warmupOrig.isEmpty()) warmupOrig.next();
        while (!warmupRedesign.isEmpty()) warmupRedesign.next();

        int runs = 5;

        // 1. Benchmark Original Solution (PriorityQueue)
        double totalOrigMs = 0;
        long memBeforeOrig = 0, memAfterOrig = 0;
        for (int r = 0; r < runs; r++) {
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            memBeforeOrig = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            OriginalPriorityQueueTriage orig = new OriginalPriorityQueueTriage();

            long start = System.nanoTime();
            for (int i = 0; i < operations; i++) {
                orig.admit("P" + i, severities[i]);
            }
            while (!orig.isEmpty()) {
                orig.next();
            }
            long end = System.nanoTime();
            memAfterOrig = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            totalOrigMs += (end - start) / 1_000_000.0;
        }
        double avgOrigMs = totalOrigMs / runs;

        // 2. Benchmark Redesigned Solution (MultiLevelBucketTriage)
        double totalRedesignMs = 0;
        long memBeforeRedesign = 0, memAfterRedesign = 0;
        for (int r = 0; r < runs; r++) {
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            memBeforeRedesign = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            RedesignedMultiLevelBucketTriage redesign = new RedesignedMultiLevelBucketTriage();

            long start = System.nanoTime();
            for (int i = 0; i < operations; i++) {
                redesign.admit("P" + i, severities[i]);
            }
            while (!redesign.isEmpty()) {
                redesign.next();
            }
            long end = System.nanoTime();
            memAfterRedesign = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            totalRedesignMs += (end - start) / 1_000_000.0;
        }
        double avgRedesignMs = totalRedesignMs / runs;

        System.out.printf("%-30s | %-16s | %-16s | %-16s%n", "Solution", "Avg Time (ms)", "Throughput (ops/s)", "Memory Delta (KB)");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-30s | %16.2f | %16.0f | %16.1f%n", "Original (PriorityQueue Heap)", avgOrigMs, (operations * 2.0) / (avgOrigMs / 1000.0), (memAfterOrig - memBeforeOrig) / 1024.0);
        System.out.printf("%-30s | %16.2f | %16.0f | %16.1f%n", "Redesigned (5-Bucket Deques)", avgRedesignMs, (operations * 2.0) / (avgRedesignMs / 1000.0), (memAfterRedesign - memBeforeRedesign) / 1024.0);
        System.out.printf("Observed Speedup of Redesign: %.2fx faster!%n", avgOrigMs / avgRedesignMs);
        System.out.println();
    }

    public static void runStarvationValidation() {
        System.out.println("==========================================================================");
        System.out.println("VALIDATION OF ANTI-STARVATION AGING MECHANISM");
        System.out.println("Scenario: 1 minor patient (S5) arrives at t=0; continuous flood of S1/S2 cases");
        System.out.println("==========================================================================");

        RedesignedMultiLevelBucketTriage triage = new RedesignedMultiLevelBucketTriage();
        triage.admit("Patient_Asha_Minor", 5);

        int streamCount = 500;
        int treatedBeforeAsha = 0;
        boolean ashaTreated = false;

        for (long tick = 1; tick <= streamCount; tick++) {
            // High severity patient arrives
            triage.admit("Emergency_" + tick, 1 + (int)(tick % 2));

            // Apply aging with wait threshold = 50 ticks
            triage.applyAging(tick, 50);

            // Doctor treats one patient per tick
            Patient treated = triage.next();
            if (treated != null) {
                if (treated.name.equals("Patient_Asha_Minor")) {
                    ashaTreated = true;
                    System.out.printf("-> Asha (originally S5) treated at tick %d after %d high-priority cases!%n", tick, treatedBeforeAsha);
                    System.out.printf("   Final Severity at Treatment: S%d (Promoted via O(1) Aging)%n", treated.currentSeverity);
                    break;
                }
                treatedBeforeAsha++;
            }
        }

        System.out.println("Verification: " + (ashaTreated ? "STARVATION COMPLETELY PREVENTED (SUCCESS)" : "FAILED (Starvation occurred)"));
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Q7 DAY-4 DESIGN CHALLENGE: TRIAGE QUEUE REDESIGN & VALIDATION ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println();

        // Throughput comparison across multiple data scales
        runThroughputBenchmark(10000);
        runThroughputBenchmark(50000);
        runThroughputBenchmark(100000);

        // Validation of fairness and anti-starvation objective
        runStarvationValidation();
    }
}
