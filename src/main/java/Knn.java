/**
 * Created by pyn on 2018/4/2.
 */
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.util.Collector;
import www.pyn.bean.*;
import www.pyn.tools.Tools;

import java.util.*;
import java.util.List;

@SuppressWarnings("serial")
// 目前使用Knn-8的方法
public class Knn {
    private static HashMap<Integer, Position> trainPosition;
    private static HashMap<Integer, Direction> trainDirection;
    private static GridData[][][] trainData;
    private DataSet<PrimitiveData> testDataDS;
    private HashMap<Integer, PrimitiveData> testData;

    private static HashMap<Integer,Result> trainResult;
    private static HashMap<Integer,Result> testResult;


    private ExecutionEnvironment env;
    private ParameterTool params;
    private String writoTofile = "E:\\DataSet\\knnScore.csv";

    public Knn(ParameterTool params, ExecutionEnvironment env, PrepareData prepareData,
               PrepareResult prepareResult) {
        this.params = params;
        this.env = env;

        trainPosition = prepareData.getTrainPosition();
        trainDirection = prepareData.getTrainDirection();
        trainData = prepareData.getTrainData();
        testDataDS = prepareData.getTestDataDS();
        testData = prepareData.getTestData();

        trainResult = prepareResult.getTrainResult();
        testResult = prepareResult.getTestResult();
    }

    public DataSet<Tuple3<Integer, Double, Double>> solveKnn() {
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
//        www.pyn.tools.Tools.expandTrainSet(scores, testData, testResult, 13674);

        scores.writeAsCsv(writoTofile,"\n",",", FileSystem.WriteMode.OVERWRITE)
                .setParallelism(1);

        try {
            env.execute("FlinkScores");
        } catch (Exception e) {

        }finally {
            return scores;
        }
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
            List<Integer> visibleObjList = new ArrayList<Integer>();
            visibleObjList.clear();
//            int positionK = www.pyn.tools.Tools.Configuration.getInstance().getKnnPositionk();
//            int directionK = www.pyn.tools.Tools.Configuration.getInstance().getKnnDirectionk();
//            List<SimilarityTuple> kNearestNeighbors = www.pyn.tools.Tools.getNearestNeighbors(trainPosition, position, directionK,
//                    1, positionK, trainDirection, direction);
//
//            for(int i = 0; i < kNearestNeighbors.size(); i++) {
//                int simId = kNearestNeighbors.get(i).dataId;
////                System.out.println(dataId + " " + simId + " " + kNearestNeighbors[i].simlarity);
//                Result rs = trainResult.get(simId);
//                visibleObjList.addAll(rs.getVisibleObj());
//            }
            int gridX = SceneInfo.ToGridX(position.getPx());
            int gridY = SceneInfo.ToGridY(position.getPy());
            int gridZ = SceneInfo.ToGridZ(position.getPz());
//            List<GridData> neighbors6 = SceneInfo.nearestNeighbors6(gridX, gridY, gridZ, trainData);
            List<GridData> neighbors = SceneInfo.nearestNeighbors(gridX, gridY, gridZ, position, trainData);
//            List<GridData> neighbors = SceneInfo.nearestNeighborsByPos(gridX, gridY, gridZ, position, trainData);

            //取neighbors的前2个方向最近的
            List<Integer> trainDataId = SceneInfo.getResultFromNeighborGrid(neighbors, direction);
            for(int i = 0; i < trainDataId.size(); i++) {
                int id = trainDataId.get(i);
                visibleObjList.addAll(trainResult.get(id).getVisibleObj());
            }

//
//             for(GridData gridData : neighbors) {
//                int idx = -1;
//                double maxSim = -1;
//                for (int i = 0; i < gridData.primitives.size(); i++) {
//                    double[] d = gridData.primitives.get(i).getDirection().getDirection();
//                    double sim = Tools.vectorSimlarity(d, direction.getDirection());
//                    if (sim > maxSim) {
//                        maxSim = sim;
//                        idx = i;
//                    }
//                }
//                visibleObjList.addAll(trainResult.get(gridData.primitives.get(idx).getDataId()).getVisibleObj());
//            }
            collector.collect(new Result(dataId, Tools.removeDuplicateFromList(visibleObjList)));
            System.out.println("dataId : " + dataId);
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
