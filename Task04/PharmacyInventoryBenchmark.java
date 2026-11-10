package Task04;

import java.util.*;

/**
 * Task04 - Choosing a Map for a Pharmacy Inventory
 * 
 * Compares HashMap, TreeMap, and LinkedHashMap across:
 *  - 100,000 stock lookups (get)
 *  - Report 1 generation: Medicines in code order
 *  - Report 2 generation: Medicines in insertion order
 *  - Extra work demonstration for "wrong" map
 *  - Binary search alternative benchmark
 * 
 * Student Register Number: 192521325
 */
public class PharmacyInventoryBenchmark {

    static class Medicine implements Comparable<Medicine> {
        final String code;
        final String name;
        int stock;
        final double unitPrice;

        Medicine(String code, String name, int stock, double unitPrice) {
            this.code = code;
            this.name = name;
            this.stock = stock;
            this.unitPrice = unitPrice;
        }

        @Override
        public int compareTo(Medicine o) {
            return this.code.compareTo(o.code);
        }

        @Override
        public String toString() {
            return code + ":" + name + "(" + stock + ")";
        }
    }

    public static List<Medicine> generateMedicines(int n) {
        List<Medicine> list = new ArrayList<>(n);
        // Generate pseudo-random codes in arbitrary insertion order
        Random r = new Random(1337);
        for (int i = 0; i < n; i++) {
            int codeNum = r.nextInt(1_000_000);
            String code = String.format("MED-%06d", codeNum);
            list.add(new Medicine(code, "Drug_" + i, 50 + r.nextInt(500), 10.0 + r.nextDouble() * 100));
        }
        return list;
    }

    public static void runBenchmark(int size) {
        System.out.println("==========================================================================");
        System.out.printf("PHARMACY INVENTORY BENCHMARK: N = %,d medicines%n", size);
        System.out.println("==========================================================================");

        List<Medicine> dataset = generateMedicines(size);
        int lookupCount = 100_000;
        String[] queryKeys = new String[lookupCount];
        Random rand = new Random(42);
        for (int i = 0; i < lookupCount; i++) {
            queryKeys[i] = dataset.get(rand.nextInt(size)).code;
        }

        // -------------------------------------------------------------
        // 1. Populate Maps
        // -------------------------------------------------------------
        Map<String, Medicine> hashMap = new HashMap<>();
        Map<String, Medicine> treeMap = new TreeMap<>();
        Map<String, Medicine> linkedHashMap = new LinkedHashMap<>();

        long start = System.nanoTime();
        for (Medicine m : dataset) hashMap.put(m.code, m);
        double hashPutTime = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        for (Medicine m : dataset) treeMap.put(m.code, m);
        double treePutTime = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        for (Medicine m : dataset) linkedHashMap.put(m.code, m);
        double lhmPutTime = (System.nanoTime() - start) / 1_000_000.0;

        // -------------------------------------------------------------
        // 2. 100,000 Lookups (get)
        // -------------------------------------------------------------
        start = System.nanoTime();
        long hashStockSum = 0;
        for (String k : queryKeys) hashStockSum += hashMap.get(k).stock;
        double hashLookupTime = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        long treeStockSum = 0;
        for (String k : queryKeys) treeStockSum += treeMap.get(k).stock;
        double treeLookupTime = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        long lhmStockSum = 0;
        for (String k : queryKeys) lhmStockSum += linkedHashMap.get(k).stock;
        double lhmLookupTime = (System.nanoTime() - start) / 1_000_000.0;

        // -------------------------------------------------------------
        // 3. Report 1: Code Order (Alphabetical by code)
        // -------------------------------------------------------------
        // TreeMap: Natural iteration
        start = System.nanoTime();
        List<Medicine> treeReport1 = new ArrayList<>(treeMap.values());
        double treeReport1Time = (System.nanoTime() - start) / 1_000_000.0;

        // HashMap: Needs sorting! (Extra work)
        start = System.nanoTime();
        List<Medicine> hashReport1 = new ArrayList<>(hashMap.values());
        Collections.sort(hashReport1); // O(N log N) sort
        double hashReport1Time = (System.nanoTime() - start) / 1_000_000.0;

        // LinkedHashMap: Needs sorting! (Extra work)
        start = System.nanoTime();
        List<Medicine> lhmReport1 = new ArrayList<>(linkedHashMap.values());
        Collections.sort(lhmReport1);
        double lhmReport1Time = (System.nanoTime() - start) / 1_000_000.0;

        // -------------------------------------------------------------
        // 4. Report 2: Insertion Order
        // -------------------------------------------------------------
        // LinkedHashMap: Natural iteration
        start = System.nanoTime();
        List<Medicine> lhmReport2 = new ArrayList<>(linkedHashMap.values());
        double lhmReport2Time = (System.nanoTime() - start) / 1_000_000.0;

        // HashMap & TreeMap: CANNOT produce insertion order without external metadata tracking!
        // Simulating the extra work: tracking an insertion sequence list or storing timestamp in Medicine
        start = System.nanoTime();
        // Fallback: extracting values only (does not match insertion order!)
        List<Medicine> hashReport2 = new ArrayList<>(hashMap.values());
        double hashReport2Time = (System.nanoTime() - start) / 1_000_000.0;

        // -------------------------------------------------------------
        // 5. Alternative Concept: Sorted Array + Arrays.binarySearch
        // -------------------------------------------------------------
        Medicine[] sortedArray = dataset.toArray(new Medicine[0]);
        Arrays.sort(sortedArray);
        Medicine dummyKey = new Medicine("", "", 0, 0);

        start = System.nanoTime();
        long arrayStockSum = 0;
        for (String k : queryKeys) {
            int idx = Arrays.binarySearch(sortedArray, new Medicine(k, "", 0, 0));
            if (idx >= 0) arrayStockSum += sortedArray[idx].stock;
        }
        double binarySearchTime = (System.nanoTime() - start) / 1_000_000.0;

        System.out.printf("%-16s | %-12s | %-16s | %-16s | %-16s%n", "Map Structure", "Put Time(ms)", "100k Gets(ms)", "Rep1:CodeOrder", "Rep2:InsertOrder");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-16s | %12.2f | %16.2f | %13.2f ms* | %13.2f ms^%n", "HashMap", hashPutTime, hashLookupTime, hashReport1Time, hashReport2Time);
        System.out.printf("%-16s | %12.2f | %16.2f | %13.2f ms  | %13s%n", "TreeMap", treePutTime, treeLookupTime, treeReport1Time, "CANNOT (No order)");
        System.out.printf("%-16s | %12.2f | %16.2f | %13.2f ms* | %13.2f ms%n", "LinkedHashMap", lhmPutTime, lhmLookupTime, lhmReport1Time, lhmReport2Time);
        System.out.printf("%-16s | %12s | %16.2f | %13s | %13s%n", "Sorted Array", "O(N) insert", binarySearchTime, "0 ms (Already)", "CANNOT");
        System.out.println("(* indicates extra O(N log N) post-sort work was required)");
        System.out.println("(^ indicates order is scrambled/unsupported without secondary metadata)");
        System.out.printf("Single Lookup Latency: HashMap = %.3f µs, TreeMap = %.3f µs, BinSearch = %.3f µs%n",
                (hashLookupTime * 1000.0) / lookupCount,
                (treeLookupTime * 1000.0) / lookupCount,
                (binarySearchTime * 1000.0) / lookupCount);
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Q4 BENCHMARK: HASHMAP vs TREEMAP vs LINKEDHASHMAP FOR PHARMACY ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println();

        runBenchmark(1000);
        runBenchmark(10000);
        runBenchmark(100000);
    }
}
