import org.apache.commons.math3.analysis.function.Min;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import www.pyn.bean.SimilarityTuple;

import java.util.ArrayList;
import java.util.List;

class PQ {
    double acc;
    double recall;
    public PQ(double acc, double recall) {
        this.acc = acc;
        this.recall = recall;
    }
}

public class Main {

    public static void main(String[] args) throws Exception {

        ExecutionEnvironment env;
        ParameterTool params;

        params = ParameterTool.fromArgs(args);
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);

        PrepareData prepareData = PrepareData.getInstance(env);
        PrepareResult prepareResult = PrepareResult.getInstance(env);

        CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(params, env, prepareData, prepareResult);
        collaborativeFiltering.solveCollaborativeFiltering();

//        Recommender recommender = new Recommender(params, env, prepareData, prepareResult);
//        recommender.solveRecommender();
//        Knn knn = new Knn(params, env, prepareData, prepareResult);
//        knn.solveKnn();
//        knn.test();
//

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
