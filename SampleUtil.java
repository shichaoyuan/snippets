import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * 
 * 随机采样
 * 
 * @author yuanshichao
 *
 */
public class SampleUtil {
    
    public static <T> List<T> sampleUniformRandom(List<T> population, int k) {
        if (population == null) {
            return null;
        }
        
        int n = population.size();
        if (k < 0 || k > n) {
            return null;
        }
        
        List<T> result = new ArrayList<T>(k);
        int setsize = 21;
        if (k > 5) {
            setsize += (int)Math.pow(4, Math.ceil(Math.log(k*3)/Math.log(4)));
        }
        Random random = new Random();
        if (n <= setsize) {
            List<T> pool = new ArrayList<T>(population);
            for (int i = 0; i < k; i++) {
                int j = random.nextInt(n-i);
                result.add(pool.get(j));
                pool.set(j, pool.get(n-i-1));
            }
        } else {
            Set<Integer> selected = new HashSet<Integer>();
            for (int i = 0; i < k; i++) {
                int j = random.nextInt(n);
                while(selected.contains(j)) {
                    j = random.nextInt(n);
                }
                selected.add(j);
                result.add(population.get(j));
            }
        }
        return result;
    }
    
    public static <T> List<T> sampleReservoir(Iterator<T> iter, int k) {
        if (iter == null) {
            return null;
        }

        if (k <= 0) {
            return null;
        }

        List<T> result = new LinkedList<T>();
        Random random = new Random();
        int i = 0;
        while (iter.hasNext()) {
            if (i < k) {
                result.add(iter.next());
            } else {
                int r = random.nextInt(i+1);
                if (r < k) {
                    result.set(r, iter.next());
                } else {
                    iter.next();
                }
            }
            i++;
        }
        return result;
    }
    
    public static <T> List<SampledItem<T>> sampleAES(Iterator<WeightedItem<T>> iter, int k) {
        if (iter == null) {
            return null;
        }

        if (k <= 0) {
            return null;
        }

        Queue<SampledItem<T>> minHeap = new PriorityQueue<SampledItem<T>>(k);

        Random random = new Random();
        for (int i = 0; i < k; i++) {
            if (iter.hasNext()) {
                WeightedItem<T> item = iter.next();
                SampledItem<T> sampledItem = new SampledItem<T>(item.getItem(), genSortedKey(random, item.getWeight()));
                minHeap.add(sampledItem);
            }
        }

        while (iter.hasNext()) {
            WeightedItem<T> weightedItem = iter.next();
            double sortedKey = genSortedKey(random, weightedItem.getWeight());
            SampledItem<T> minItem = minHeap.peek();
            if (sortedKey > minItem.getSortedKey()) {
                minHeap.poll();
                SampledItem<T> sampledItem = new SampledItem<T>(weightedItem.getItem(), sortedKey);
                minHeap.add(sampledItem);
            }
        }
        List<SampledItem<T>> sampledItemList =  new ArrayList<SampledItem<T>>(minHeap);
        Collections.sort(sampledItemList, Collections.reverseOrder());
        return sampledItemList;
    }
    
    private static double genSortedKey(Random random, double weight) {
        return Math.pow(random.nextDouble(), 1.0/weight);
    }
    
    public static class WeightedItem<T> {
        private T item;
        private double weight;
        
        public WeightedItem(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }

        public T getItem() {
            return item;
        }

        public double getWeight() {
            return weight;
        }
        
    }
    
    public static class SampledItem<T> implements Comparable<SampledItem<T>> {
        private T item;
        private double sortedKey;
        
        public SampledItem(T item, double sortedKey) {
            this.item = item;
            this.sortedKey = sortedKey;
        }

        public T getItem() {
            return item;
        }

        public double getSortedKey() {
            return sortedKey;
        }

        @Override
        public int compareTo(SampledItem<T> that) {
            return Double.valueOf(this.sortedKey).compareTo(Double.valueOf(that.getSortedKey()));
        }
        
    }
}
