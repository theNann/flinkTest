import org.apache.commons.math3.analysis.function.Min;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import www.pyn.bean.SimilarityTuple;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
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
        String ip = "10.222.163.208" ;
        int port = 6001;

//        Socket socket = new Socket(ip, port);
//        System.out.println("连上服务器");
//        InputStream input = socket.getInputStream();
//        byte[] res = new byte[10];
//        int len = input.read(res);
//        System.out.println("len , ret : " + len + " " + new String(res));
//        input.close();
//        socket.close();

        ExecutionEnvironment env;
        ParameterTool params;

        params = ParameterTool.fromArgs(args);
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);

        PrepareData prepareData = PrepareData.getInstance(env);
        PrepareResult prepareResult = PrepareResult.getInstance(env);
//        System.out.println("train_size " + prepareData.getTrainPosition().size());
//        System.out.println("result_size " + prepareResult.getTrainResult().size());

        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> text = senv.socketTextStream(ip, port, "\n");
        text.map(new MapFunction<String, Object>() {
            public Object map(String s) throws Exception {

                char[] chs = s.toCharArray();
//                for(int i = 0; i < chs.length; i++) {
//                    System.out.println((int)chs[i]);
//                }
                int i = 0;
                while(i < 68) {
                    int num = (chs[i+3] << 24) | (chs[i+2] << 16) | (chs[i+1] << 8) | (chs[i]);
                    double num1 = num*1.0/1000000;
                    i += 4;
                    System.out.println("num1 : " + num1);
                }
                return 10;
            }
        });
        senv.execute("test");


//        CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(params, env, prepareData, prepareResult);
//        collaborativeFiltering.solveCollaborativeFiltering();

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
