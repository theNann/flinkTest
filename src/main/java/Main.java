import org.apache.commons.math3.analysis.function.Min;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SocketTextStreamFunction;
import org.apache.flink.util.Collector;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import www.pyn.bean.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PQ {
    double acc;
    double recall;
    public PQ(double acc, double recall) {
        this.acc = acc;
        this.recall = recall;
    }
}

public class Main {

    public static PrimitiveData generatePrimitiveData(double[] data) {
        Matrix viewMatrix = DenseMatrix.Factory.zeros(4,4);
        int idx = 2;
        for(int i = 0; i < viewMatrix.getRowCount(); i++) {
            for(int j = 0; j < viewMatrix.getColumnCount(); j++) {
                viewMatrix.setAsDouble(data[idx++], i ,j);
            }
        }
        Matrix bMatrix = DenseMatrix.Factory.zeros(4,4);
        idx = 2;
        for(int i = 0; i < bMatrix.getRowCount(); i++) {
            for(int j = 0; j < bMatrix.getColumnCount(); j++) {
                bMatrix.setAsDouble(data[idx++], i ,j);
            }
        }
        for(int i = 0; i < 3; i++) {
            bMatrix.setAsDouble(0, 3, i);
        }
        Matrix tMatrix = viewMatrix.mtimes(bMatrix.inv());
        PrimitiveData primitiveData = new PrimitiveData((int)data[0],
                -tMatrix.getAsDouble(3,0),  -tMatrix.getAsDouble(3,1), -tMatrix.getAsDouble(3,2),
                viewMatrix.getAsDouble(0, 2), viewMatrix.getAsDouble(1, 2), viewMatrix.getAsDouble(2, 2));
        return primitiveData;
//        System.out.println(viewMatrix);
//        System.out.println(bMatrix);
//        System.out.println(primitiveData);
    }

    public static void generaterBuffer(byte[] buffer, int idx, int data) {
        buffer[idx*4] = (byte) (data & 0xff);
        buffer[idx*4+1] = (byte) ((data >> 8 ) & 0xff);
        buffer[idx*4+2] = (byte) ((data >> 16 ) & 0xff);
        buffer[idx*4+3] = (byte) ((data >> 24 ) & 0xff);
    }

    public static void main(String[] args) throws Exception {
        String ip = "10.222.163.208" ;
//        String ip = "127.0.0.1" ;
        int port = 6001;

        ExecutionEnvironment env;
        ParameterTool params;

        params = ParameterTool.fromArgs(args);
        env = ExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);

        PrepareData prepareData = PrepareData.getInstance(env);
        PrepareResult prepareResult = PrepareResult.getInstance(env);

//        CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(params, env, prepareData, prepareResult);
//        collaborativeFiltering.solveCollaborativeFiltering();
//
//        Recommender recommender = new Recommender(params, env, prepareData, prepareResult);
//        recommender.solveRecommender();

        Knn knn = new Knn(params, env, prepareData, prepareResult);
//        knn.solveKnn();

//        System.out.println("train_size " + prepareData.getTrainPosition().size());
//        System.out.println("result_size " + prepareResult.getTrainResult().size());

        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<byte[]> bytes = senv.addSource(new SocketByteStreamFunction(ip, port, 18*4,0L));

        DataStream<Result> result = bytes.flatMap(new FlatMapFunction<byte[], Result>() {
            public void flatMap(byte[] bytes, Collector<Result> collector) throws Exception {
//                System.out.println("size : " + bytes.length);
                int idx = 0;
                double[] data = new double[18];
                int i = 0;
                while(i < bytes.length) {
                    int num = ((bytes[i+3]&0xff) << 24) | ((bytes[i+2]&0xff) << 16) | ((bytes[i+1]&0xff) << 8) | (bytes[i]&0xff);
                    double num1 = num*1.0/1000000;
                    i += 4;
                    data[idx++] = num1;
//                    System.out.println("num1 : " + num1);
                }
                PrimitiveData primitiveData = generatePrimitiveData(data);
//                System.out.println(primitiveData);
                int dataId = primitiveData.getDataId();
                Position position = primitiveData.getPosition();
                Direction direction = primitiveData.getDirection();
//            System.out.println("testDataId : " + dataId + " " + position + " " + direction);
                Set<Integer> visibleObjSet = new HashSet<Integer>();
                visibleObjSet.clear();
                int k = 3;
                List<SimilarityTuple> kNearestNeighbors = Tools.getNearestNeighbors(Knn.getTrainPosition(), position, k,
                        1, 15, Knn.getTrainDirection(), direction);

                for(i = 0; i < kNearestNeighbors.size(); i++) {
                    int simId = kNearestNeighbors.get(i).dataId;
//                System.out.println(dataId + " " + simId + " " + kNearestNeighbors[i].simlarity);
                    Result rs = Knn.getTrainResult().get(simId);
                    visibleObjSet.addAll(rs.getVisibleObj());
                }
                collector.collect(new Result(dataId,new ArrayList<Integer>(visibleObjSet)));
                System.out.println("dataId : " + dataId);
            }
        });

        result.writeToSocket(ip, 6002, new SerializationSchema<Result>() {
            public byte[] serialize(Result re) {
                int len = re.getVisibleObj().size()+1;
                System.out.println("len : " + len);
                byte[] buffer = new byte[(len+1)*4];
                generaterBuffer(buffer, 0, len);
                generaterBuffer(buffer, 1, re.getDataId());
                for(int i = 0; i < re.getVisibleObj().size(); i++) {
                    int tmp = re.getVisibleObj().get(i);
                    generaterBuffer(buffer, i+2, tmp);
                }
//                File file = new File("/home/pyn/Desktop/out.txt");
//                FileOutputStream in;
//                try {
//                    in = new FileOutputStream(file);
//                    String start = "start ";
//                    in.write(start.getBytes());
//                    for(int i = 0; i < re.getVisibleObj().size(); i++) {
//                        int tmp = re.getVisibleObj().get(i);
//                        byte[] bt = String.valueOf(tmp).concat(", ").getBytes();
//                        in.write(bt, 0 ,bt.length);
//                        generaterBuffer(buffer, i+2, tmp);
//                    }
//                    in.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                byte[] buffer = new byte[4*3];
//                generaterBuffer(buffer, 0, 2);
//                generaterBuffer(buffer, 1, 0);
//                generaterBuffer(buffer, 2, 1);

                return buffer;
            }
        });
        senv.execute("test");




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

    public static final class sendMap implements MapFunction<Result, Object> {
        public Object map(Result result) throws Exception {

            return null;
        }
    }




}
