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
import www.pyn.bean.*;

import java.util.*;

@SuppressWarnings("serial")

public class Knn {
    private static HashMap<Integer, Position> trainPosition;
    private static HashMap<Integer, Direction> trainDirection;
    private DataSet<PrimitiveData> testDataDS;
    private PrepareData prepareData;

    private static HashMap<Integer,Result> trainResult;
    private static HashMap<Integer,Result> testResult;
    private PrepareResult prepareResult;

    private String[] args;
    private ExecutionEnvironment env;
    private ParameterTool params;

    public Knn(String[] args) {
        this.args = args;
        params = ParameterTool.fromArgs(args);
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);

        prepareData = PrepareData.getInstance(env);
        trainPosition = prepareData.getTrainPosition();
        trainDirection = prepareData.getTrainDirection();
        testDataDS = prepareData.getTestDataDS();

        prepareResult = PrepareResult.getInstance(env);
        trainResult = prepareResult.getTrainResult();
        testResult = prepareResult.getTestResult();
    }
    public void test() {
        System.out.println("train " + trainPosition.size() + " " + trainDirection.size() + " " + trainResult.size());
        System.out.println("test " + testResult.size());
        for(Map.Entry<Integer,Result> entry : testResult.entrySet()) {
            System.out.println(entry.getValue());
        }
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
        scores.writeAsCsv("/home/pyn/Desktop/BIMRecommed/output/flinkScores.csv","\n",",")
                .setParallelism(1);
        try {
            env.execute("FlinkScores");
        } catch (Exception e) {}
    }

    public static final class scoreMap implements FlatMapFunction<Result, Tuple3<Integer,Double, Double>> {
        public void flatMap(Result result, Collector<Tuple3<Integer, Double, Double>> collector) throws Exception {
            int dataId = result.getDataId();
            Set<Integer> preditcVisibleObj = result.getVisibleObj();
            Set<Integer> targetVisibleObj = testResult.get(dataId).getVisibleObj();
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
//            System.out.println("testDataId : " + dataId);
            Set<Integer> visibleObjSet = new HashSet<Integer>();
            visibleObjSet.clear();
            int k = 5;
            SimilarityTuple[] kNearestNeighbors = Tools.getNearestNeighbors(trainPosition, position, k,
                    false, 15, trainDirection, direction);
            for(int i = 0; i < k; i++) {
                int simId = kNearestNeighbors[i].dataId;
//                System.out.println("dataId : " + dataId + " " + simId);
                Result rs = trainResult.get(simId);
                visibleObjSet.addAll(rs.getVisibleObj());
            }
            collector.collect(new Result(dataId,visibleObjSet));
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

    public static void main(String[] args) throws Exception {
        Knn knn = new Knn(args);
        knn.solveKnn();
//        knn.test();
//        final ParameterTool params = ParameterTool.fromArgs(args);
//        // set up the execution environment
//        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
//
//        // make parameters available in the web interface
//        env.getConfig().setGlobalJobParameters(params);
//
//        List<Position> trainPosition = null;
//        System.out.println("Input!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//        DataSet<Position> dataTrainPositionDataSet = env.readCsvFile("E:\\pyn_playground\\flink_data.csv")
//                .pojoType(Position.class, "dataId", "px", "py", "pz");
//        trainPosition = dataTrainPositionDataSet.collect();
        // 也可以通过如下方式得到dataTrainPosition，即先读为Tuple4，然后通过map转化为Dataset<position>
//        final DataSet<Tuple4<Integer,Integer,Integer,Integer>> dataTrainPositionDataSet =
//                env.readCsvFile("E:\\pyn_playground\\flink_data.csv").
//                types(Integer.class, Integer.class, Integer.class, Integer.class);

//        trainPosition = dataTrainPositionDataSet.flatMap(new FlatMapFunction<Tuple4<Integer,Integer,Integer,Integer>, Position>() {
//            public void flatMap(Tuple4<Integer,Integer,Integer,Integer> tuple4, Collector<Position> out) {
//                out.collect(new Position(tuple4.f0, tuple4.f1, tuple4.f2, tuple4.f3));
//                System.out.println(tuple4.f0 +" " + tuple4.f1 + " " + tuple4.f2 + " " + tuple4.f3 + "MAPPPPPPPPPPPPPP");
//            }
//        }).collect();





//        DataSet<String> text = env.fromElements("hello world");
//        DataSet<Tuple2<String, Integer>> counts =
//                // split up the lines in pairs (2-tuples) containing: (word,1)
//                text.flatMap(new SocketWordCount.Tokenizer())
//                        // group by the tuple field "0" and sum up tuple field "1"
//                        .groupBy(0)
//                        .sum(1);
//
//        if (params.has("output")) {
//            counts.writeAsCsv(params.get("output"), "\n", " ");
//            // execute program
//            env.execute("Knn");
//        } else {
//            System.out.println("Printing result to stdout. Use --output to specify output path.");
//            counts.print();
//        }
    }


}
