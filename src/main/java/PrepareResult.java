import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.util.Collector;
import www.pyn.bean.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pyn on 2018/4/17.
 */
public class PrepareResult {
    private final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    private List<Result> trainResult;
    private List<Result> testResult;
    private String trainFilePath = "E:\\BIMRecommed\\input\\target_train.txt";
    private String testFilePath = "E:\\BIMRecommed\\input\\target_test.txt";
    private static PrepareResult prepareResult = null;

    private PrepareResult() {
        readTrainResult();
        readTestResult();
    }

    public static PrepareResult getInstance() {
        if(prepareResult == null) {
            prepareResult = new PrepareResult();
        }
        return prepareResult;
    }

    public void readTrainResult() {
        this.trainResult = readResult(trainFilePath);
    }
    public void readTestResult() {
        this.testResult = readResult(testFilePath);
    }

    public List<Result> readResult(String filePath) {
        DataSet<String> lines = env.readTextFile(filePath);
        DataSet<Result> ds = lines.flatMap(new FlatMapFunction<String, Result>() {
            public void flatMap(String s, Collector<Result> collector) throws Exception {
                String[] split = s.split(", ");
                int dataId = Integer.valueOf(split[0]);
                List<Integer> visibleObj = new ArrayList<Integer>();
                for(int i = 1; i < split.length; i++) {
                    visibleObj.add(Integer.valueOf(split[i]));
                }
                Result result = new Result(dataId, visibleObj);
                collector.collect(result);
            }
        });
        try {
            return ds.collect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Result> getTrainResult() {
        return trainResult;
    }

    public List<Result> getTestResult() {
        return testResult;
    }
}
