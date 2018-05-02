import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.util.Collector;
import www.pyn.bean.*;

import java.util.*;

public class Recommender {
    private static HashMap<Integer, Position> trainPosition;
    private static HashMap<Integer, Direction> trainDirection;
    private DataSet<PrimitiveData> testDataDS;
    private PrepareData prepareData;

    private static HashMap<Integer,Result> trainResult;
    private static HashMap<Integer,Result> testResult;
    private PrepareResult prepareResult;

    private ExecutionEnvironment env;
    private ParameterTool params;

    public Recommender(ParameterTool params, ExecutionEnvironment env, PrepareData prepareData,
                       PrepareResult prepareResult) {
        this.params = params;
        this.env = env;

        prepareData = prepareData;
        trainPosition = prepareData.getTrainPosition();
        trainDirection = prepareData.getTrainDirection();
        testDataDS = prepareData.getTestDataDS();

        prepareResult = prepareResult;
        trainResult = prepareResult.getTrainResult();
        testResult = prepareResult.getTestResult();
    }

    public void solveRecommender() {
        DataSet<Result> ans = testDataDS.flatMap(new recommenderMap());
        DataSet<Tuple3<Integer, Double, Double>> scores = ans.flatMap(new scoreMap());
        scores.writeAsCsv("/home/pyn/Desktop/BIMRecommed/output/flinkScores.csv","\n",",")
                .setParallelism(1);
        try {
            env.execute("FlinkScores");
        } catch (Exception e) {}
    }

    public static final class recommenderMap implements FlatMapFunction<PrimitiveData, Result> {
        public void flatMap(PrimitiveData primitiveData, Collector<Result> collector) throws Exception {
            int k = 3;
            int howMany = 2;
            int maxK = 15;
            int dataId = primitiveData.getDataId();
            Position position = primitiveData.getPosition();
            Direction direction = primitiveData.getDirection();
            Set<Integer> visibleObjSet = new HashSet<Integer>();
            visibleObjSet.clear();
            //KNN
            List<SimilarityTuple> kNearestNeighbors = Tools.getNearestNeighbors(trainPosition, position, k,
                    1, maxK, trainDirection, direction);
            //CollaborativeFiltering
            for(int i = 0; i < kNearestNeighbors.size(); i++) {
                int simId = kNearestNeighbors.get(i).dataId;
                List<Integer> visibleObj = trainResult.get(simId).getVisibleObj();
                List<SimilarityTuple> recommendNearestNeighbors = Tools.userBasedRecommend(trainResult, visibleObj, howMany);
                for(int j = 0; j < recommendNearestNeighbors.size(); j++) {
                    int _simId = recommendNearestNeighbors.get(j).dataId;
                    List<Integer> _visibleObj = trainResult.get(_simId).getVisibleObj();
                    visibleObjSet.addAll(_visibleObj);
                }
            }
            collector.collect(new Result(dataId, new ArrayList<Integer>(visibleObjSet)));
        }
    }

    public static final class scoreMap implements FlatMapFunction<Result, Tuple3<Integer, Double, Double>> {
        public void flatMap(Result result, Collector<Tuple3<Integer, Double, Double>> collector) throws Exception {
            List<Integer> preditcVisibleObj = result.getVisibleObj();
            int dataId = result.getDataId();
            System.out.println("testDataId : " + dataId);
            List<Integer> targetVisibleObj = testResult.get(dataId).getVisibleObj();
            int jiaoSize = Tools.intersection(preditcVisibleObj, targetVisibleObj);
            double acc = jiaoSize*1.0 / preditcVisibleObj.size();
            double recall = jiaoSize*1.0 / targetVisibleObj.size();
            collector.collect(new Tuple3<Integer, Double, Double>(dataId, acc, recall));
        }
    }
}
