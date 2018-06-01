/**
 * Created by pyn on 2018/4/2.
 */
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.util.Collector;
import org.omg.CORBA.Environment;
import scala.concurrent.Promise;
import www.pyn.bean.*;

import java.util.*;

@SuppressWarnings("serial")
// 目前使用Knn-8的方法
public class Knn {
    private static HashMap<Integer, Position> trainPosition;
    private static HashMap<Integer, Direction> trainDirection;
    private DataSet<PrimitiveData> testDataDS;
    private HashMap<Integer, PrimitiveData> testData;

    private static HashMap<Integer,Result> trainResult;
    private static HashMap<Integer,Result> testResult;


    private ExecutionEnvironment env;
    private ParameterTool params;
    private String writoTofile = "/home/pyn/Desktop/DataSet/knnScore.csv";

    public Knn(ParameterTool params, ExecutionEnvironment env, PrepareData prepareData,
               PrepareResult prepareResult) {
        this.params = params;
        this.env = env;

        trainPosition = prepareData.getTrainPosition();
        trainDirection = prepareData.getTrainDirection();
        testDataDS = prepareData.getTestDataDS();
        testData = prepareData.getTestData();

        trainResult = prepareResult.getTrainResult();
        testResult = prepareResult.getTestResult();
    }

    public void test() {
        System.out.println("train " + trainPosition.size() + " " + trainDirection.size() + " " + trainResult.size());
        System.out.println("test " + testResult.size());
//        for(Map.Entry<Integer,Direction> entry : trainDirection.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }

//        List<Position> train = new ArrayList<Position>();
//
//        train.add(new Position(0, 0, 0, 1));
//        train.add(new Position(1, 0, 1, 0));
//        train.add(new Position(2, 1, 0, 0));
//        train.add(new Position(3, 1, 1, 1));
//
//        Position p = new Position(0, 0, 0, 1);
//        SimilarityTuple[] nearestNeighbor = Tools.getNearestNeighbors(train, 3, p);
//        for(int i = 0; i < nearestNeighbor.length; i++) {
//            System.out.println(nearestNeighbor[i].dataId + " " + nearestNeighbor[i].simlarity);
//        }
//        System.out.println("testResult_size: " + testResult.size());
//        Result rs = testResult.get(0);
//        for(int i = 0; i < rs.getVisibleObj().size(); i++) {
//            System.out.println(rs.getVisibleObj().);
//        }
//        System.out.println("trainResult_size: " + trainResult.size());
//        System.out.println("dataId : " + testResult.get(0).getDataId());
//        for(int i = 0; i < testResult.get(0).getVisibleObj().size(); i++) {
//            System.out.println(testResult.get(0).getVisibleObj().get(i) + " ");
//        }
//        System.out.println("trainPosition_size: " + trainPosition.size());
//        System.out.println("trainDirection_size: " + trainDirection.size());
//        for(int i = 0; i < trainDirection.size(); i++) {
//            System.out.println(trainDirection.get(i) + "//////////////////////");
//        }
    }

    public void solveKnn() {
        DataSet<Result> ans = testDataDS.flatMap(new knnMap());
//        if (params.has("output")) {
//            System.out.println("output : " + params.get("output"));
//            ans.writeAsText(params.get("output"),FileSystem.WriteMode.OVERWRITE);
//            try {
//                env.execute("Knn");
//            } catch (Exception e) {}
//        } else {
//            System.out.println("Printing result to stdout. Use --output to specify output path.");
//            try {
//                ans.print();
//            } catch (Exception e) {}
//        }

        DataSet<Tuple3<Integer, Double, Double>> scores = ans.flatMap(new scoreMap());


//        HashMap<Integer, Result> testTarget = prepareResult.getTestResult();
//        System.out.println("testTarget size : " + testTarget.size());
//        HashMap<Integer, PrimitiveData> testData = prepareData.getTestData();
//        System.out.println("testData size : " + testData.size());
//        Tools.expandTrainSet(scores, testData, testResult, 9472);

        scores.writeAsCsv(writoTofile,"\n",",", FileSystem.WriteMode.OVERWRITE)
                .setParallelism(1);

        try {
            env.execute("FlinkScores");
        } catch (Exception e) {}
    }

    public static final class scoreMap implements FlatMapFunction<Result, Tuple3<Integer,Double, Double>> {
        public void flatMap(Result result, Collector<Tuple3<Integer, Double, Double>> collector) throws Exception {
            int dataId = result.getDataId();
            List<Integer> preditcVisibleObj = result.getVisibleObj();
            System.out.println("testDataId : " + dataId);
            List<Integer> targetVisibleObj = testResult.get(dataId).getVisibleObj();
            int jiaoSize = Tools.intersection(preditcVisibleObj, targetVisibleObj);
            double acc = jiaoSize*1.0 / preditcVisibleObj.size();
            double recall = jiaoSize*1.0 / targetVisibleObj.size();
            collector.collect(new Tuple3<Integer, Double, Double>(dataId, acc, recall));
        }
    }

    public static final class knnMap implements FlatMapFunction<PrimitiveData, Result> {
        public void flatMap(PrimitiveData primitiveData, Collector<Result> collector) throws Exception {
            int dataId = primitiveData.getDataId();
            Position position = primitiveData.getPosition();
            Direction direction = primitiveData.getDirection();
//            System.out.println("testDataId : " + dataId + " " + position + " " + direction);
            Set<Integer> visibleObjSet = new HashSet<Integer>();
            visibleObjSet.clear();
            int k = 3;
            List<SimilarityTuple> kNearestNeighbors = Tools.getNearestNeighbors(trainPosition, position, k,
                    1, 15, trainDirection, direction);

            for(int i = 0; i < kNearestNeighbors.size(); i++) {
                int simId = kNearestNeighbors.get(i).dataId;
//                System.out.println(dataId + " " + simId + " " + kNearestNeighbors[i].simlarity);
                Result rs = trainResult.get(simId);
                visibleObjSet.addAll(rs.getVisibleObj());
            }
            collector.collect(new Result(dataId,new ArrayList<Integer>(visibleObjSet)));
//            System.out.println("dataId : " + dataId);
        }
    }

    public static HashMap<Integer, Position> getTrainPosition() {
        return trainPosition;
    }

    public static void setTrainPosition(HashMap<Integer, Position> trainPosition) {
        Knn.trainPosition = trainPosition;
    }

    public static HashMap<Integer, Direction> getTrainDirection() {
        return trainDirection;
    }

    public static void setTrainDirection(HashMap<Integer, Direction> trainDirection) {
        Knn.trainDirection = trainDirection;
    }

    public DataSet<PrimitiveData> getTestDataDS() {
        return testDataDS;
    }

    public void setTestDataDS(DataSet<PrimitiveData> testDataDS) {
        this.testDataDS = testDataDS;
    }

    public static HashMap<Integer, Result> getTrainResult() {
        return trainResult;
    }
}
