
import org.apache.commons.math3.linear.ArrayRealVector;
import org.omg.CORBA.INTERNAL;
import www.pyn.bean.Direction;
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

    public static int intersection(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> jiao = new HashSet<Integer>();
        jiao.clear();
        jiao.addAll(set1);
        jiao.retainAll(set2);
        return jiao.size();
    }

    public static double setSimilarity(Set<Integer> set1, Set<Integer> set2) {
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

    public static SimilarityTuple[] getNearestNeighbors(HashMap<Integer, Position> trainPosition, Position position,
                           int minK, boolean considerDirectiton, int maxK, HashMap<Integer, Direction>trainDirection,
                                                        Direction direction) {
        List<SimilarityTuple> similarityTuples = new ArrayList<SimilarityTuple>();
        for(Map.Entry<Integer,Position> entry : trainPosition.entrySet()) {
            int dataId = entry.getKey();
            double sim = Tools.vectorSimlarity(entry.getValue().getPosition(), position.getPosition());
            similarityTuples.add(new SimilarityTuple(dataId, sim));
        }
        Collections.sort(similarityTuples, new Comparator<SimilarityTuple>() {
            public int compare(SimilarityTuple o1, SimilarityTuple o2) {
                if(o2.simlarity > o1.simlarity) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        int kk;
        if(considerDirectiton) {
            kk = maxK;
        } else {
            kk = minK;
        }
        SimilarityTuple[] nearestNeighbors = new SimilarityTuple[kk];
        for(int i = 0; i < kk; i++) {
            nearestNeighbors[i] = similarityTuples.get(i);
        }
        if(!considerDirectiton) {
            return nearestNeighbors;
        } else {
            similarityTuples.clear();
            for(int i = 0; i < kk; i++) {
                int dataId = nearestNeighbors[i].dataId;
                double sim = Tools.vectorSimlarity(trainDirection.get(dataId).getDirection(), direction.getDirection());
                similarityTuples.add(new SimilarityTuple(dataId, sim));
            }
            Collections.sort(similarityTuples, new Comparator<SimilarityTuple>() {
                public int compare(SimilarityTuple o1, SimilarityTuple o2) {
                    if(o2.simlarity > o1.simlarity) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            SimilarityTuple[] res = new SimilarityTuple[minK];
            for(int i = 0; i < minK; i++) {
                res[i] = similarityTuples.get(i);
            }
            return res;
        }
    }
}
