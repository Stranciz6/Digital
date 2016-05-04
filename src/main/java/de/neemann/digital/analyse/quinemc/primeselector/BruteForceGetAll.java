package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * @author hneemann
 */
public class BruteForceGetAll implements PrimeSelector, PrimeSelectorGetAll {

    private ArrayList<ArrayList<TableRow>> foundSolutions;

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

        int primesUsed = 0;

        foundSolutions = new ArrayList<>();

        ArrayList<Integer> indicesOpen = new ArrayList<>();
        for (int mask : list) {

            if (primesUsed != 0 && Integer.bitCount(mask) > primesUsed)
                break;

            indicesOpen.clear();
            indicesOpen.addAll(termIndices);
            int m = mask;
            for (TableRow aPrimesAvail : primesAvail) {
                if ((m & 1) > 0) {
                    indicesOpen.removeAll(aPrimesAvail.getSource());
                }
                m >>= 1;
            }
            if (indicesOpen.isEmpty()) {
                primesUsed = Integer.bitCount(mask);

                ArrayList<TableRow> singleSolution = new ArrayList<>(primes);
                m = mask;
                for (TableRow aPrime : primesAvail) {
                    if ((m & 1) > 0) {
                        singleSolution.add(aPrime);
                    }
                    m >>= 1;
                }
                foundSolutions.add(singleSolution);
            }
        }
        primes.clear();
        primes.addAll(foundSolutions.get(0));
    }

    @Override
    public ArrayList<ArrayList<TableRow>> getAllSolutions() {
        return foundSolutions;
    }
}
