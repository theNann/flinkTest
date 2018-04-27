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
//        List<PQ> list = new ArrayList<PQ>();
//        list.add(new PQ(0.9381,0.9683));
//        list.add(new PQ(0.9079525972,0.984799448757129));
//        list.add(new PQ(0.885084556384266,0.990190570165258));
//
//        list.add(new PQ(0.924188739379066,0.975516945297042));
//        list.add(new PQ(0.895636158948888,0.987652718470316));
//        list.add(new PQ(0.873493429041373,0.991625639614306));
//        for(int i = 0; i < list.size(); i++) {
//            double res = Tools.calF(list.get(i).acc, list.get(i).recall);
//            System.out.println("res : " + res);
//        }
//        MinHeap minHeap = new MinHeap(3);
//        for(int i = 0; i < 3; i++) {
//            minHeap.add(new SimilarityTuple(i, i*0.1));
//        }
//        minHeap.buildHeap();
//
//        for(int i = 0; i < minHeap.arr.length; i++) {
//            System.out.println(minHeap.arr[i].dataId + " " + minHeap.arr[i].similarityP);
//        }
//        System.out.println();
//
//        for(int i = 3; i < 7; i++) {
//            if(i*0.1  > minHeap.arr[0].similarityP) {
//                minHeap.arr[0] = new SimilarityTuple(i , i*0.1);
//                minHeap.adjustHeap(0);
//                System.out.println("adjust");
//                for(int j = 0; j < minHeap.arr.length; j++) {
//                    System.out.println(minHeap.arr[j].dataId + " " + minHeap.arr[j].similarityP);
//                }
//            }
//        }
//
//        for(int i = 0; i < minHeap.arr.length; i++) {
//            System.out.println(minHeap.arr[i].dataId + " " + minHeap.arr[i].similarityP);
//        }
        ExecutionEnvironment env;
        ParameterTool params;

        params = ParameterTool.fromArgs(args);
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);

        PrepareData prepareData = PrepareData.getInstance(env);
        PrepareResult prepareResult = PrepareResult.getInstance(env);
//
//        Recommender recommender = new Recommender(params, env, prepareData, prepareResult);
//        recommender.solveRecommender();
//        Knn knn = new Knn(params, env, prepareData, prepareResult);
//        knn.solveKnn();
//        knn.test();
//
        CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(params, env, prepareData, prepareResult);
        collaborativeFiltering.solveCollaborativeFiltering();


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
