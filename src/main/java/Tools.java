
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
        int kk;
        if(considerDirectiton == 1) {
            kk = maxK;
        } else {
            kk = minK;
        }
//        List<SimilarityTuple> similarityTuples = new ArrayList<SimilarityTuple>();
        MinHeap minHeap = new MinHeap(kk);
        for(Map.Entry<Integer,Position> entry : trainPosition.entrySet()) {
            int dataId = entry.getKey();
            double sim = Tools.euclideanDistanceSim(entry.getValue().getPosition(), position.getPosition());
            if(minHeap.getCount() < kk) {
                minHeap.add(new SimilarityTuple(dataId, sim));
                if(minHeap.getCount() == kk) {
                    minHeap.buildHeap();
                }
            } else {
                if(sim > minHeap.arr[0].similarityP) {
                    minHeap.arr[0] = new SimilarityTuple(dataId, sim);
                    minHeap.adjustHeap(0);
                }
            }
        }
        /*
        前15大： 3082 0.3239528344648195
前15大： 5693 0.44326841161747826
前15大： 577 0.3484965087575313
前15大： 7719 0.48682421019260047
前15大： 1764 0.6172192773740202
前15大： 5014 0.37494338882550643
前15大： 3284 0.37155020800284505
前15大： 9153 0.7008986415954628
前15大： 5324 0.48843160567314436
前15大： 7308 0.8313174461293918
前15大： 1486 0.7063195267556713
前15大： 9338 0.5479543171566342
前15大： 4560 0.40521440070228054
前15大： 9088 0.8211664535859425
前15大： 1927 0.4060304625815459

         */
        for(int i = 0; i < minHeap.arr.length; i++) {
            System.out.println("前15大： " + minHeap.arr[i].dataId + " " + minHeap.arr[i].similarityP);
        }

        List<SimilarityTuple> res = new ArrayList<SimilarityTuple>();
        if(considerDirectiton == 0) {
            for(int i = 0; i < minHeap.arr.length; i++) {
                res.add(minHeap.arr[i]);
            }
            return res;
        } else {
            //若考虑Direction，就对前maxK个数据进行simD和simP二维排序，选出simD前minK大的数据作为最后结果，
            // 同时也要去除那些direction夹角大于fov的数据（这个比较重要，能够提升recall），所以最后的结果个数小于等于minK。
            List<SimilarityTuple> similarityTuplesNew = new ArrayList<SimilarityTuple>();
            int maxSimPIndex = 0;
            double maxSimp = minHeap.arr[0].similarityP;

            for(int i = 0; i < minHeap.arr.length; i++) {
                int dataId = minHeap.arr[i].dataId;
                double simP = minHeap.arr[i].similarityP;
                double simD = Tools.vectorSimlarity(trainDirection.get(dataId).getDirection(), direction.getDirection());
                similarityTuplesNew.add(new SimilarityTuple(dataId, simP, simD));
                if(maxSimp < simP) {
                    maxSimp = simP;
                    maxSimPIndex = i;
                }
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

            for(int i = 0; i < similarityTuplesNew.size(); i++) {
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
                res.add(minHeap.arr[maxSimPIndex]);
            }
            return res;
        }
    }

    public static List<SimilarityTuple> userBasedRecommend(HashMap<Integer, Result> trainResult, Set<Integer> predictVisibleObj,
                                                           int howMany) {
        MinHeap minHeap = new MinHeap(howMany);
        int count = 0;
        for(Map.Entry<Integer, Result> entry : trainResult.entrySet()) {
            int dataId = entry.getKey();
            Set<Integer> visibleObj = entry.getValue().getVisibleObj();
            double sim = Tools.setSimilarity(predictVisibleObj, visibleObj);
            //当只有一个sim时调用第一个构造函数，虽然是similarityP，这里也可认为是结果集合的相似性
            if(count < howMany) {
                minHeap.add(new SimilarityTuple(dataId, sim));
            } else if(count == howMany) {
                minHeap.buildHeap();
            } else {
                if(sim > minHeap.arr[0].similarityP) {
                    minHeap.arr[0] = new SimilarityTuple(dataId, sim);
                    minHeap.adjustHeap(0);
                }
            }
            count += 1;
        }
        List<SimilarityTuple> userSimilarity = new ArrayList<SimilarityTuple>();
        for(int i = 0; i < minHeap.arr.length; i++) {
            userSimilarity.add(minHeap.arr[i]);
        }
        return userSimilarity;
    }

    public static double calF1(double acc, double recall) {
        return 2*acc*recall / (acc + recall);
    }
}
