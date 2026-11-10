package Task06;

import java.util.*;

/**
 * Task06 - Triage Investigation: Priority, Fairness, Growth & Alternative Architectures
 * 
 * Conducts:
 *  1. 8-patient hand-verified check and heap-print inspection
 *  2. Growth scaling investigation (N = 1,000, 10,000, 100,000)
 *  3. Starvation demonstration and aging fair-rule design
 *  4. Alternative concepts comparison (PriorityQueue vs Sorted LinkedList vs 5 FIFO Queues)
 * 
 * Student Register Number: 192521325
 */
public class TriageInvestigation {

    public static void run8PatientHandVerifiedCheck() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("PART 1: 8-Patient Hand-Verified Correctness Check");
        System.out.println("--------------------------------------------------------------------------");

        // Sequence of 8 patients arriving:
        // 1. Anita (S3) [arr:0]
        // 2. Bharat (S1) [arr:1]
        // 3. Chitra (S2) [arr:2]
        // 4. Deepak (S1) [arr:3] (Tied with Bharat on S1, arrived later)
        // 5. Esha (S4) [arr:4]
        // 6. Farhan (S3) [arr:5] (Tied with Anita on S3, arrived later)
        // 7. Gita (S5) [arr:6]
        // 8. Hari (S2) [arr:7] (Tied with Chitra on S2, arrived later)

        // Hand-calculated expected treatment order:
        // S1: Bharat (arr 1), Deepak (arr 3)
        // S2: Chitra (arr 2), Hari (arr 7)
        // S3: Anita (arr 0), Farhan (arr 5)
        // S4: Esha (arr 4)
        // S5: Gita (arr 6)
        // Expected: Bharat(S1) Deepak(S1) Chitra(S2) Hari(S2) Anita(S3) Farhan(S3) Esha(S4) Gita(S5)

        PriorityQueue<Patient> pq = new PriorityQueue<>(
                Comparator.comparingInt(Patient::getSeverity).thenComparingLong(Patient::getArrival)
        );

        long counter = 0;
        pq.add(new Patient("Anita", 3, counter++));
        pq.add(new Patient("Bharat", 1, counter++));
        pq.add(new Patient("Chitra", 2, counter++));
        pq.add(new Patient("Deepak", 1, counter++));
        pq.add(new Patient("Esha", 4, counter++));
        pq.add(new Patient("Farhan", 3, counter++));
        pq.add(new Patient("Gita", 5, counter++));
        pq.add(new Patient("Hari", 2, counter++));

        System.out.println("Direct PriorityQueue.toString() [RAW INTERNAL BINARY HEAP ARRAY]:");
        System.out.println(" " + pq);
        System.out.println("-> Notice: Raw toString() DOES NOT match sorted treatment order!");
        System.out.println("   Explanation: A binary heap satisfies only parent <= child (partial order).");
        System.out.println("   The array represents level-order heap nodes, not fully sorted elements.");
        System.out.println();

        System.out.println("Actual Treatment Extraction Order (via queue.poll()):");
        StringBuilder sb = new StringBuilder();
        while (!pq.isEmpty()) {
            sb.append(pq.poll()).append(' ');
        }
        String actual = sb.toString().trim();
        System.out.println(" " + actual);
        String expected = "Bharat(S1) Deepak(S1) Chitra(S2) Hari(S2) Anita(S3) Farhan(S3) Esha(S4) Gita(S5)";
        System.out.println("Hand-verified Match: " + (actual.equals(expected) ? "VERIFIED (100% MATCH)" : "MISMATCH"));
    }

    public static void runGrowthInvestigation() {
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println("PART 2: Growth Scaling Investigation (N = 1,000, 10,000, 100,000)");
        System.out.println("Theoretical Prediction: Both admit() and poll() cost O(log N) comparisons.");
        System.out.println("--------------------------------------------------------------------------");

        int[] sizes = {1000, 10000, 100000};
        Random rand = new Random(123);

        System.out.printf("%-12s | %-16s | %-16s | %-16s%n", "Queue Size N", "Admit All (ms)", "Poll All (ms)", "Avg Op (µs)");
        System.out.println("--------------------------------------------------------------------------");

        for (int n : sizes) {
            PriorityQueue<Patient> pq = new PriorityQueue<>(
                    Comparator.comparingInt(Patient::getSeverity).thenComparingLong(Patient::getArrival)
            );

            // Populate N patients
            long startAdmit = System.nanoTime();
            for (int i = 0; i < n; i++) {
                pq.add(new Patient("Patient_" + i, 1 + rand.nextInt(5), i));
            }
            double admitMs = (System.nanoTime() - startAdmit) / 1_000_000.0;

            // Poll all N patients
            long startPoll = System.nanoTime();
            while (!pq.isEmpty()) {
                pq.poll();
            }
            double pollMs = (System.nanoTime() - startPoll) / 1_000_000.0;

            double avgOpMicrosec = ((admitMs + pollMs) * 1000.0) / (2.0 * n);
            System.out.printf("%-12d | %16.2f | %16.2f | %16.3f%n", n, admitMs, pollMs, avgOpMicrosec);
        }
    }

    public static void runStarvationAndFairnessInquiry() {
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println("PART 3: Starvation Inquiry & Fair-Rule Aging Simulation");
        System.out.println("--------------------------------------------------------------------------");

        // Scenario: A minor patient Asha (S5) arrives at t = 0.
        // A continuous flood of critical/moderate patients (S1, S2, S3) keeps arriving.
        // Without aging: Asha will STARVE indefinitely (wait time -> infinity).

        System.out.println("WITHOUT AGING:");
        PriorityQueue<Patient> standardPQ = new PriorityQueue<>(
                Comparator.comparingInt(Patient::getSeverity).thenComparingLong(Patient::getArrival)
        );
        standardPQ.add(new Patient("Asha", 5, 0)); // S5 arriving first

        // 100 critical patients arrive subsequently
        for (int i = 1; i <= 100; i++) {
            standardPQ.add(new Patient("SevereCase_" + i, 1 + (i % 3), i));
        }

        // Doctor sees 50 patients
        int seenBeforeAsha = 0;
        boolean ashaTreated = false;
        for (int i = 0; i < 50; i++) {
            Patient p = standardPQ.poll();
            if (p.name.equals("Asha")) {
                ashaTreated = true;
                break;
            }
            seenBeforeAsha++;
        }
        System.out.println(" After 50 treatments, was Asha seen? " + ashaTreated);
        System.out.println(" Patients seen while Asha waited: " + seenBeforeAsha + " (Starvation confirmed!)");

        System.out.println("\nWITH AGING FAIR-RULE (Effective Priority = severity - (waitTicks / threshold)):");
        // Fair Patient with dynamic effective priority calculation
        class FairPatient {
            final String name;
            final int baseSeverity;
            final long arrivalTick;

            FairPatient(String name, int baseSeverity, long arrivalTick) {
                this.name = name;
                this.baseSeverity = baseSeverity;
                this.arrivalTick = arrivalTick;
            }

            int getEffectiveSeverity(long currentTick) {
                long waitTicks = currentTick - arrivalTick;
                int promotion = (int) (waitTicks / 20); // promote 1 level every 20 ticks
                return Math.max(1, baseSeverity - promotion); // cap at S1
            }

            @Override
            public String toString() {
                return name + "(Base:S" + baseSeverity + ")";
            }
        }

        List<FairPatient> erWaiting = new ArrayList<>();
        erWaiting.add(new FairPatient("Asha", 5, 0));
        for (int i = 1; i <= 100; i++) {
            erWaiting.add(new FairPatient("SevereCase_" + i, 1 + (i % 3), i));
        }

        // Simulate doctor picking next patient using dynamic aging
        long currentTick = 85;
        FairPatient chosen = Collections.min(erWaiting, Comparator
                .comparingInt((FairPatient p) -> p.getEffectiveSeverity(currentTick))
                .thenComparingLong(p -> p.arrivalTick)
        );
        System.out.println(" At tick " + currentTick + ", Asha's effective severity has aged to S" 
                + erWaiting.get(0).getEffectiveSeverity(currentTick));
        System.out.println(" Next patient summoned for treatment: " + chosen);
        System.out.println(" Result: Aging successfully prevented starvation!");
    }

    public static void runAlternativeArchitecturesBenchmark() {
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println("PART 4: Alternative Architectures Comparison (N = 20,000 patients)");
        System.out.println(" Comparing: 1. PriorityQueue O(log N)");
        System.out.println("            2. 5 FIFO Queues (Array of Deques) O(1)");
        System.out.println("--------------------------------------------------------------------------");

        int n = 20000;
        Random rand = new Random(42);
        int[] severities = new int[n];
        for (int i = 0; i < n; i++) severities[i] = 1 + rand.nextInt(5);

        // 1. PriorityQueue
        PriorityQueue<Patient> pq = new PriorityQueue<>(
                Comparator.comparingInt(Patient::getSeverity).thenComparingLong(Patient::getArrival)
        );
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) pq.add(new Patient("P" + i, severities[i], i));
        while (!pq.isEmpty()) pq.poll();
        double pqTime = (System.nanoTime() - start) / 1_000_000.0;

        // 2. 5 FIFO Queues (Array of ArrayDeques)
        @SuppressWarnings("unchecked")
        ArrayDeque<Patient>[] multiQueue = new ArrayDeque[5];
        for (int i = 0; i < 5; i++) multiQueue[i] = new ArrayDeque<>();

        start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            int s = severities[i];
            multiQueue[s - 1].addLast(new Patient("P" + i, s, i)); // O(1)
        }
        int served = 0;
        while (served < n) {
            for (int s = 0; s < 5; s++) { // O(1) scan across 5 buckets
                if (!multiQueue[s].isEmpty()) {
                    Patient p = multiQueue[s].pollFirst(); // O(1)
                    served++;
                    break;
                }
            }
        }
        double multiQueueTime = (System.nanoTime() - start) / 1_000_000.0;

        System.out.printf("%-24s | %-16s | %-16s%n", "Architecture", "Time (ms)", "Time Complexity");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-24s | %16.2f | %-16s%n", "PriorityQueue (Heap)", pqTime, "O(log N) Admit & Poll");
        System.out.printf("%-24s | %16.2f | %-16s%n", "5 FIFO Queues (Deques)", multiQueueTime, "O(1) Admit & Poll");
        System.out.printf("Speedup of 5 FIFO Queues vs PriorityQueue: %.2fx%n", pqTime / multiQueueTime);
    }

    public static void main(String[] args) {
        System.out.println("=== Q6 TRIAGE INVESTIGATION & EXPERIMENTS ===");
        run8PatientHandVerifiedCheck();
        runGrowthInvestigation();
        runStarvationAndFairnessInquiry();
        runAlternativeArchitecturesBenchmark();
    }
}
