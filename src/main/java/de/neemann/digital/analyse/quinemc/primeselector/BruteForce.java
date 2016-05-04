package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * @author hneemann
 */
public class BruteForce implements PrimeSelector {
    @Override
    public void select(ArrayList<TableRow> primes, ArrayList<TableRow> primesAvail, TreeSet<Integer> termIndices) {
        int comb = 1 << primesAvail.size();
        ArrayList<Integer> list = new ArrayList<>(comb);
        for (int i = 1; i < comb; i++) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                return Integer.bitCount(i1) - Integer.bitCount(i2);
            }
        });

        ArrayList<Integer> l = new ArrayList<>();
        for (int mask : list) {
            l.addAll(termIndices);
            int m = mask;
            for (TableRow aPrimesAvail : primesAvail) {
                if ((m & 1) > 0) {
                    l.removeAll(aPrimesAvail.getSource());
                }
                m >>= 1;
            }
            if (l.isEmpty()) {
                m = mask;
                for (TableRow aPrime : primesAvail) {
                    if ((m & 1) > 0) {
                        primes.add(aPrime);
                    }
                    m >>= 1;
                }
                return;
            } else {
                l.clear();
            }
        }
        throw new RuntimeException("BruteForce Error!");
    }
}
