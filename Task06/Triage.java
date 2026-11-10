package Task06;

import java.util.Comparator;
import java.util.PriorityQueue;

class Patient {
    final String name;
    final int severity;
    final long arrival;

    Patient(String name, int severity, long arrival) {
        this.name = name;
        this.severity = severity;
        this.arrival = arrival;
    }

    public int getSeverity() { return severity; }
    public long getArrival() { return arrival; }

    @Override
    public String toString() {
        return name + "(S" + severity + ")";
    }
}

public class Triage {
    private long counter = 0;

    // TODO 1: Create a PriorityQueue that serves the lowest severity number first and,
    // for equal severity, the earliest arrival first
    private final PriorityQueue<Patient> queue = new PriorityQueue<>(
            Comparator.comparingInt(Patient::getSeverity)
                      .thenComparingLong(Patient::getArrival)
    );

    void admit(String name, int severity) {
        queue.add(new Patient(name, severity, counter++));
    }

    Patient next() {
        return queue.poll();
    }

    public static void main(String[] args) {
        Triage er = new Triage();
        er.admit("Ravi", 3);
        er.admit("Meena", 1);
        er.admit("John", 3);
        er.admit("Asha", 5);
        er.admit("Kumar", 1);
        er.admit("Lata", 2);

        StringBuilder order = new StringBuilder();
        Patient p;
        while ((p = er.next()) != null) {
            order.append(p).append(' ');
        }
        System.out.println(order.toString().trim());
    }
}
