/**
 * Created by pyn on 2018/4/2.
 */
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.util.Collector;
import www.pyn.bean.Direction;
import www.pyn.bean.Position;
import www.pyn.bean.Result;
import www.pyn.bean.SimilarityTuple;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")

public class Knn {
    private static List<Position> trainPosition;
    private static List<Direction> trainDirection;
    private DataSet<Position> testPositionDS;
    private DataSet<Direction> testDirectionDS;
    private PrepareData prepareData;

    private List<Result> trainResult;
    private List<Result> testResult;
    private PrepareResult prepareResult;

    public Knn() {
        prepareData = PrepareData.getInstance();
        trainPosition = prepareData.getTrainPosition();
        trainDirection = prepareData.getTrainDirection();
        testPositionDS = prepareData.getTestPositionDS();
        testDirectionDS = prepareData.getTestDirectionDS();

        prepareResult = PrepareResult.getInstance();
        trainResult = prepareResult.getTrainResult();
        testResult = prepareResult.getTestResult();
    }
    public void test() {
        List<Position> train = new ArrayList<Position>();

        train.add(new Position(0, 0, 0, 1));
        train.add(new Position(1, 0, 1, 0));
        train.add(new Position(2, 1, 0, 0));
        train.add(new Position(3, 1, 1, 1));

        Position p = new Position(0, 0, 0, 1);
        SimilarityTuple[] nearestNeighbor = Tools.getNearestNeighbors(train, 3, p);
        for(int i = 0; i < nearestNeighbor.length; i++) {
            System.out.println(nearestNeighbor[i].dataId + " " + nearestNeighbor[i].simlarity);
        }
//        System.out.println("testResult_size: " + testResult.size());
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
//    public void solveKnn() {
//        DataSet<Tuple2<Integer,List<Tuple2<Integer,Double>>>> ans = testPositionDS.flatMap(new knnMap());
//        try {
//            ans.print();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    public static List<Position> getTrainPosition() {
        return trainPosition;
    }

    public static void setTrainPosition(List<Position> trainPosition) {
        Knn.trainPosition = trainPosition;
    }

    public static List<Direction> getTrainDirection() {
        return trainDirection;
    }

    public static void setTrainDirection(List<Direction> trainDirection) {
        Knn.trainDirection = trainDirection;
    }

    public DataSet<Position> getTestPositionDS() {
        return testPositionDS;
    }

    public void setTestPositionDS(DataSet<Position> testPositionDS) {
        this.testPositionDS = testPositionDS;
    }

    public DataSet<Direction> getTestDirectionDS() {
        return testDirectionDS;
    }

    public void setTestDirectionDS(DataSet<Direction> testDirectionDS) {
        this.testDirectionDS = testDirectionDS;
    }

    public static void main(String[] args) throws Exception {
        Knn knn = new Knn();
//        knn.solveKnn();
        knn.test();
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
    public static final class knnMap implements FlatMapFunction<Position, Tuple2<Integer, List<Tuple2<Integer,Double>>>> {
        public void flatMap(Position position, Collector<Tuple2<Integer, List<Tuple2<Integer,Double>>>> collector) throws Exception {
            double sum = position.getPx() + position.getPy() + position.getPz();
            List<Tuple2<Integer,Double>> ans = new ArrayList<Tuple2<Integer, Double>>();
            for(int i = 0; i < trainPosition.size(); i++) {
                double sum_tmp = trainPosition.get(i).getPx() + trainPosition.get(i).getPy() + trainPosition.get(i).getPz();
                int id_tmp = trainPosition.get(i).getDataId();
                ans.add(new Tuple2<Integer,Double>(id_tmp, sum_tmp-sum));
            }
            collector.collect(new Tuple2<Integer, List<Tuple2<Integer,Double>>>(position.getDataId(), ans));
        }
    }

}
