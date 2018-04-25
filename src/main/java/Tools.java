
import org.apache.commons.math3.linear.ArrayRealVector;
import org.omg.CORBA.INTERNAL;
import www.pyn.bean.Direction;
import www.pyn.bean.Position;
import www.pyn.bean.Result;
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
//        return 0.5 + 0.5 * cos;
        return cos;
    }

    public static double euclideanDistanceSim(double[] v1, double[] v2) {
        ArrayRealVector vec1 = new ArrayRealVector(v1);
        ArrayRealVector vec2 = new ArrayRealVector(v2);
        ArrayRealVector tmp = vec1.subtract(vec2);
        double dist = tmp.getNorm();
        double sim = 1.0 / (1.0+dist);
        return sim;
    }

    public static int intersection(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> jiao = new HashSet<Integer>();
        jiao.clear();
        jiao.addAll(set1);
        jiao.retainAll(set2);
        return jiao.size();
    }

    public static double setSimilarity(Set<Integer> set1, Set<Integer> set2) {
        if(set1.size() == 0 && set2.size() == 0) {
            return 0;
        }
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

    public static List<SimilarityTuple> listSort(List<SimilarityTuple> list) {
        Collections.sort(list, new Comparator<SimilarityTuple>() {
            public int compare(SimilarityTuple o1, SimilarityTuple o2) {
                if(o2.similarityP > o1.similarityP) {
                    return 1;
                } else if(o2.similarityP == o1.similarityP){
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return list;
    }
    public static List<SimilarityTuple> getNearestNeighbors(HashMap<Integer, Position> trainPosition, Position position,
                           int minK, int considerDirectiton, int maxK, HashMap<Integer, Direction>trainDirection,
                                                        Direction direction) {
        List<SimilarityTuple> similarityTuples = new ArrayList<SimilarityTuple>();
        for(Map.Entry<Integer,Position> entry : trainPosition.entrySet()) {
            int dataId = entry.getKey();
            double sim = Tools.euclideanDistanceSim(entry.getValue().getPosition(), position.getPosition());
            similarityTuples.add(new SimilarityTuple(dataId, sim));
        }
        similarityTuples = Tools.listSort(similarityTuples);
        int kk;
        if(considerDirectiton == 1) {
            kk = maxK;
        } else {
            kk = minK;
        }
        if(considerDirectiton == 0) {
            return similarityTuples.subList(0, kk);
        } else {
            //若考虑Direction，就对前maxK个数据进行simD和simP二维排序，选出simD前minK大的数据作为最后结果，
            // 同时也要去除那些direction夹角大于fov的数据（这个比较重要，能够提升recall），所以最后的结果个数小于等于minK。
            List<SimilarityTuple> similarityTuplesNew = new ArrayList<SimilarityTuple>();
            for(int i = 0; i < kk; i++) {
                int dataId = similarityTuples.get(i).dataId;
                double simP = similarityTuples.get(i).similarityP;
                double simD = Tools.vectorSimlarity(trainDirection.get(dataId).getDirection(), direction.getDirection());
                similarityTuplesNew.add(new SimilarityTuple(dataId, simP, simD));
            }
            Collections.sort(similarityTuplesNew, new Comparator<SimilarityTuple>() {
                public int compare(SimilarityTuple o1, SimilarityTuple o2) {
                    if(o2.similarityD > o1.similarityD) {
                        return 1;
                    } else if(o2.similarityD == o1.similarityD) {
                        if(o1.similarityP > o2.similarityP) {
                            return 1;
                        } else {
                            return 0;
                        }
                    } else {
                        return -1;
                    }
                }
            });
            List<SimilarityTuple> res = new ArrayList<SimilarityTuple>();
            for(int i = 0; i < kk; i++) {
                if(similarityTuplesNew.get(i).similarityD < Math.cos(1.0472)) {
                    break;
                } else {
                    res.add(similarityTuplesNew.get(i));
                }
                if(res.size() == minK) {
                    break;
                }
            }
            if(res.size() == 0) {
                res.add(similarityTuples.get(0));
            }
            return res;
        }
    }

    public static List<SimilarityTuple> userBasedRecommend(HashMap<Integer, Result> trainResult, Set<Integer> predictVisibleObj,
                                                           int topK) {
        List<SimilarityTuple> userSimilarity = new ArrayList<SimilarityTuple>();
        for(Map.Entry<Integer, Result> entry : trainResult.entrySet()) {
            int dataId = entry.getKey();
            Set<Integer> visibleObj = entry.getValue().getVisibleObj();
            double sim = Tools.setSimilarity(predictVisibleObj, visibleObj);
            //当只有一个sim时调用第一个构造函数，虽然是similarityP，这里也可认为是结果集合的相似性
            userSimilarity.add(new SimilarityTuple(dataId, sim));
        }
        userSimilarity = Tools.listSort(userSimilarity);
        return userSimilarity.subList(0, topK);
    }
}
