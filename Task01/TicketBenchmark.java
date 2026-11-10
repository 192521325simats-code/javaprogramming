package Task01;

import java.util.*;

/**
 * Task01 - Building Text Under Load: String (+), StringBuilder, or StringBuffer?
 * 
 * Compares three text construction strategies across multiple workloads:
 *  1. String concatenation using '+' operator inside a loop
 *  2. StringBuilder (unsynchronized, resizable buffer)
 *  3. StringBuffer (synchronized, thread-safe buffer)
 * 
 * Also benchmarks alternative concepts: String.join(), StringJoiner, and String.format().
 * 
 * Student Register Number: 192521325
 */
public class TicketBenchmark {

    // Helper data for generating ticket fields
    private static final String[] FIELD_NAMES = {
        "PNR", "TrainNo", "TrainName", "Origin", "Destination", "JourneyDate",
        "Class", "Quota", "Coach", "Berth", "BerthType", "Passenger1",
        "Age1", "Gender1", "Status1", "Passenger2", "Age2", "Gender2",
        "Status2", "BaseFare", "ReservationFee", "SuperfastCharge", "GST",
        "TotalFare", "PaymentMode", "TransactionId", "BookingTime", "GateNo",
        "Platform", "DepartureTime", "ArrivalTime", "DistanceKm", "FoodChoice",
        "InsuranceOpted", "IdType", "IdNumber", "ContactMobile", "ContactEmail",
        "Concession", "CurrentStatus", "ChartPrepared", "CateringCharge",
        "DynamicFare", "TravelTime", "EmergencyContact", "QrCodeData",
        "TermsAccepted", "BoardingStation", "ReservationUpto", "TicketCollectorCode"
    };

    public static List<String> generateFields(int count) {
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String key = FIELD_NAMES[i % FIELD_NAMES.length] + "_" + (i + 1);
            String val = "VAL" + (i * 37 + 101);
            list.add(key + "=" + val);
        }
        return list;
    }

    // 1. String concatenation using '+' in a loop
    public static String buildWithPlus(List<String> fields) {
        String result = "";
        for (String f : fields) {
            result = result + f + "\n";
        }
        return result;
    }

    // 2. StringBuilder
    public static String buildWithStringBuilder(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String f : fields) {
            sb.append(f).append('\n');
        }
        return sb.toString();
    }

    // 3. StringBuffer
    public static String buildWithStringBuffer(List<String> fields) {
        StringBuffer sb = new StringBuffer();
        for (String f : fields) {
            sb.append(f).append('\n');
        }
        return sb.toString();
    }

    // Alternative 1: String.join
    public static String buildWithStringJoin(List<String> fields) {
        return String.join("\n", fields) + "\n";
    }

    // Alternative 2: StringJoiner
    public static String buildWithStringJoiner(List<String> fields) {
        StringJoiner sj = new StringJoiner("\n", "", "\n");
        for (String f : fields) {
            sj.add(f);
        }
        return sj.toString();
    }

    // Alternative 3: String.format
    public static String buildWithStringFormat(List<String> fields) {
        StringBuilder formatBuilder = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            formatBuilder.append("%s\n");
        }
        return String.format(formatBuilder.toString(), fields.toArray());
    }

    public static void runWorkload(String workloadName, int numTickets, int numFields) {
        System.out.println("==========================================================================");
        System.out.printf("RUNNING WORKLOAD: %s (%d tickets, %d fields each)%n", workloadName, numTickets, numFields);
        System.out.println("==========================================================================");

        List<String> fields = generateFields(numFields);

        // Warmup JIT
        for (int i = 0; i < 500; i++) {
            buildWithPlus(fields.subList(0, Math.min(5, numFields)));
            buildWithStringBuilder(fields.subList(0, Math.min(5, numFields)));
            buildWithStringBuffer(fields.subList(0, Math.min(5, numFields)));
        }

        int runs = 5;

        // 1. String (+)
        double totalPlusMs = 0;
        long memBeforePlus = 0, memAfterPlus = 0;
        for (int r = 0; r < runs; r++) {
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            memBeforePlus = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long start = System.nanoTime();
            for (int t = 0; t < numTickets; t++) {
                String s = buildWithPlus(fields);
                if (s.isEmpty()) System.out.print("");
            }
            long end = System.nanoTime();
            memAfterPlus = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            totalPlusMs += (end - start) / 1_000_000.0;
        }
        double avgPlusMs = totalPlusMs / runs;
        // Each + creates an intermediate StringBuilder and String. In loop of n fields:
        // (n * 2) intermediate objects per ticket
        long estimatedPlusObjects = (long) numTickets * numFields * 2;

        // 2. StringBuilder
        double totalSbMs = 0;
        long memBeforeSb = 0, memAfterSb = 0;
        for (int r = 0; r < runs; r++) {
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            memBeforeSb = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long start = System.nanoTime();
            for (int t = 0; t < numTickets; t++) {
                String s = buildWithStringBuilder(fields);
                if (s.isEmpty()) System.out.print("");
            }
            long end = System.nanoTime();
            memAfterSb = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            totalSbMs += (end - start) / 1_000_000.0;
        }
        double avgSbMs = totalSbMs / runs;
        long estimatedSbObjects = (long) numTickets * 2; // 1 StringBuilder + 1 final String

        // 3. StringBuffer
        double totalSbufMs = 0;
        long memBeforeSbuf = 0, memAfterSbuf = 0;
        for (int r = 0; r < runs; r++) {
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            memBeforeSbuf = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long start = System.nanoTime();
            for (int t = 0; t < numTickets; t++) {
                String s = buildWithStringBuffer(fields);
                if (s.isEmpty()) System.out.print("");
            }
            long end = System.nanoTime();
            memAfterSbuf = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            totalSbufMs += (end - start) / 1_000_000.0;
        }
        double avgSbufMs = totalSbufMs / runs;
        long estimatedSbufObjects = (long) numTickets * 2;

        // Alternative: String.join
        double totalJoinMs = 0;
        for (int r = 0; r < runs; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < numTickets; t++) {
                String s = buildWithStringJoin(fields);
                if (s.isEmpty()) System.out.print("");
            }
            long end = System.nanoTime();
            totalJoinMs += (end - start) / 1_000_000.0;
        }
        double avgJoinMs = totalJoinMs / runs;

        // Alternative: StringJoiner
        double totalJoinerMs = 0;
        for (int r = 0; r < runs; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < numTickets; t++) {
                String s = buildWithStringJoiner(fields);
                if (s.isEmpty()) System.out.print("");
            }
            long end = System.nanoTime();
            totalJoinerMs += (end - start) / 1_000_000.0;
        }
        double avgJoinerMs = totalJoinerMs / runs;

        System.out.printf("%-18s | %-15s | %-20s | %-16s%n", "Approach", "Avg Time (ms)", "Est. Intermediate Objs", "Memory Delta (KB)");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-18s | %15.2f | %20d | %16.1f%n", "String (+)", avgPlusMs, estimatedPlusObjects, (memAfterPlus - memBeforePlus) / 1024.0);
        System.out.printf("%-18s | %15.2f | %20d | %16.1f%n", "StringBuilder", avgSbMs, estimatedSbObjects, (memAfterSb - memBeforeSb) / 1024.0);
        System.out.printf("%-18s | %15.2f | %20d | %16.1f%n", "StringBuffer", avgSbufMs, estimatedSbufObjects, (memAfterSbuf - memBeforeSbuf) / 1024.0);
        System.out.printf("%-18s | %15.2f | %20s | %16s%n", "String.join()", avgJoinMs, "N/A (O(n))", "N/A");
        System.out.printf("%-18s | %15.2f | %20s | %16s%n", "StringJoiner", avgJoinerMs, "N/A (O(n))", "N/A");
        System.out.printf("Speedup (StringBuilder vs String +): %.2fx%n", avgPlusMs / Math.max(avgSbMs, 0.001));
        System.out.println();
    }

    public static void testMultithreadedScenario() throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println("DESIGN DECISION EXPERIMENT: Multithreaded Booking Server");
        System.out.println("10 threads building tickets concurrently and appending to shared booking log");
        System.out.println("==========================================================================");

        int numThreads = 10;
        int ticketsPerThread = 2000;
        List<String> fields = generateFields(20);

        // Case A: Shared StringBuffer for log (synchronized per append)
        final StringBuffer sharedBuffer = new StringBuffer();
        Thread[] threadsA = new Thread[numThreads];
        long startA = System.nanoTime();
        for (int i = 0; i < numThreads; i++) {
            threadsA[i] = new Thread(() -> {
                for (int t = 0; t < ticketsPerThread; t++) {
                    // Per-ticket: StringBuilder (Thread-confined!)
                    String ticket = buildWithStringBuilder(fields);
                    // Shared log: StringBuffer
                    sharedBuffer.append("BOOKING_LOG: ").append(ticket.substring(0, 30)).append('\n');
                }
            });
            threadsA[i].start();
        }
        for (Thread t : threadsA) t.join();
        long timeA = (System.nanoTime() - startA) / 1_000_000;

        System.out.printf("Combined Model (StringBuilder per ticket + StringBuffer shared log): %d ms%n", timeA);
        System.out.printf("Total logged lines in sharedBuffer: %d%n", sharedBuffer.toString().split("\n").length);
        System.out.println("Defense: Per-ticket building is thread-confined, requiring zero lock acquisition (StringBuilder).");
        System.out.println("Shared log requires atomic concurrent append across threads, where StringBuffer guarantees synchronization.");
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Q1 BENCHMARK: STRING vs STRINGBUILDER vs STRINGBUFFER UNDER LOAD ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println();

        // Workload 1: Short ticket, 10,000 tickets with 10 fields each
        runWorkload("Workload 1 (Short Ticket)", 10000, 10);

        // Workload 2: Medium/Long ticket, 10,000 tickets with 50 fields each
        runWorkload("Workload 2 (50 Fields Standard Ticket)", 10000, 50);

        // Workload 3: Custom heavy workload, 20,000 tickets with 100 fields each
        runWorkload("Workload 3 (Heavy Booking Burst - 100 Fields)", 20000, 100);

        // Multithreaded simulation
        testMultithreadedScenario();
    }
}
