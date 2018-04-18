import org.apache.commons.math3.linear.ArrayRealVector;
import org.omg.CORBA.INTERNAL;
import www.pyn.bean.Position;
import www.pyn.bean.SimilarityTuple;

import java.util.*;

/**
 * Created by pyn on 2018/4/18.
 */


public class Tools {
    public static double vectorSimlarity(double[] v1, double[] v2) {
        ArrayRealVector vec1 = new ArrayRealVector(v1);
        ArrayRealVector vec2 = new ArrayRealVector(v2);
        double dotRes = vec1.dotProduct(vec2);
        double denorm = vec1.getNorm() * vec2.getNorm();
        double cos = dotRes / denorm;
        return 0.5 + 0.5 * cos;
    }

    public static double setSimilarity(List<Integer> l1, List<Integer> l2) {
        Set<Integer> set1 = new HashSet<Integer>(l1);
        Set<Integer> set2 = new HashSet<Integer>(l2);
        Set<Integer> jiao = new HashSet<Integer>();
        jiao.clear();
        jiao.addAll(set1);
        jiao.retainAll(set2);
        Set<Integer> bing = new HashSet<Integer>();
        bing.clear();
        bing.addAll(set1);
        bing.addAll(set2);
        return jiao.size()*1.0 / bing.size();
    }

    public static SimilarityTuple[] getNearestNeighbors(List<Position> trainPosition, int k, Position position) {
        List<SimilarityTuple> similarityTuples = new ArrayList<SimilarityTuple>();
        for(int i = 0; i < trainPosition.size(); i++) {
            int dataId = trainPosition.get(i).getDataId();
            double sim = Tools.vectorSimlarity(trainPosition.get(i).getPosition(), position.getPosition());
            similarityTuples.add(new SimilarityTuple(dataId, sim));
        }
        Collections.sort(similarityTuples, new Comparator<SimilarityTuple>() {
            public int compare(SimilarityTuple o1, SimilarityTuple o2) {
                if(o2.simlarity > o1.simlarity) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        SimilarityTuple[] nearestNeighbors = new SimilarityTuple[k];
        for(int i = 0; i < k; i++) {
            nearestNeighbors[i] = similarityTuples.get(i);
        }
        return nearestNeighbors;
    }
}
