import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.util.Collector;
import www.pyn.bean.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollaborativeFiltering {
    private static HashMap<Integer, Position> trainPosition;
    private static HashMap<Integer, Direction> trainDirection;
    private DataSet<PrimitiveData> testDataDS;
    private PrepareData prepareData;

    private static HashMap<Integer,Result> trainResult;
    private static HashMap<Integer,Result> testResult;
    private PrepareResult prepareResult;

    private ExecutionEnvironment env;
    private ParameterTool params;

    public CollaborativeFiltering(ParameterTool params, ExecutionEnvironment env, PrepareData prepareData,
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

    public void solveCollaborativeFiltering() {
        DataSet<Result> ans = testDataDS.flatMap(new recommenderMap());
        DataSet<Tuple3<Integer, Double, Double>> scores = ans.flatMap(new scoreMap());
        scores.writeAsCsv("/home/pyn/Desktop/BIMRecommed/output/ScoresCollaboratTmp.csv","\n",",",
                FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        try {
            env.execute("FlinkScores");
        } catch (Exception e) {}
    }

    public static final class scoreMap implements FlatMapFunction<Result, Tuple3<Integer,Double, Double>> {
        public void flatMap(Result result, Collector<Tuple3<Integer, Double, Double>> collector) throws Exception {
            Set<Integer> preditcVisibleObj = result.getVisibleObj();
            int dataId = result.getDataId();
            System.out.println("testDataId : " + dataId);
            Set<Integer> targetVisibleObj = testResult.get(dataId).getVisibleObj();
            int jiaoSize = Tools.intersection(preditcVisibleObj, targetVisibleObj);
            double acc = jiaoSize*1.0 / preditcVisibleObj.size();
            double recall = jiaoSize*1.0 / targetVisibleObj.size();
            collector.collect(new Tuple3<Integer, Double, Double>(dataId, acc, recall));
        }
    }

    public static final class recommenderMap implements FlatMapFunction<PrimitiveData, Result> {
        public void flatMap(PrimitiveData primitiveData, Collector<Result> collector) throws Exception {
            int dataId = primitiveData.getDataId();
            Position position = primitiveData.getPosition();
            Direction direction = primitiveData.getDirection();
            List<SimilarityTuple> coldStartNearestNeighbor = Tools.getNearestNeighbors(trainPosition, position, 1,
                    1, 15, trainDirection, direction);
            Set<Integer> visibleObjSet = new HashSet<Integer>();
            visibleObjSet.clear();
            int coldStartDataId = coldStartNearestNeighbor.get(0).dataId;
            for(int i = 0; i < coldStartNearestNeighbor.size(); i++) {
                int simId = coldStartNearestNeighbor.get(i).dataId;
                Result rs = trainResult.get(simId);
                visibleObjSet.addAll(rs.getVisibleObj());
            }
            int howMany = 3;
            List<SimilarityTuple> recommendNearestNeighbors = Tools.userBasedRecommend(trainResult, visibleObjSet, howMany);
            for(int i = 0; i < recommendNearestNeighbors.size(); i++) {
                int simId = recommendNearestNeighbors.get(i).dataId;
                if(simId == coldStartDataId) {
                    continue;
                }
                Result rs = trainResult.get(simId);
                visibleObjSet.addAll(rs.getVisibleObj());
            }
            collector.collect(new Result(dataId, visibleObjSet));
        }
    }
}
