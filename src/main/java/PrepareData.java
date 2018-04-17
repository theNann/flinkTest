/**
 * Created by pyn on 2018/4/3.
 */
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import www.pyn.bean.Direction;
import www.pyn.bean.Position;

import java.util.List;

//TODO 添加direction，测试输入数据的正确性（训练集和测试集）(已完成)
//TODO 从target.txt文件中读取训练集和测试集的结果，建议新建一个类PrepareResult来操作
//TODO 之后添加一个工具包，计算两个向量之前的相似度，以及两个集合之间的相似度

@SuppressWarnings("serial")
public class PrepareData {
    private final ExecutionEnvironment env;
    private List<Position> trainPosition;
    private List<Direction> trainDirection;
    private DataSet<Position> testPositionDS;
    private DataSet<Direction> testDirectionDS;
    private String trainFilePath = "E:\\BIMRecommed\\input\\data_train.csv";
    private String testFilePath = "E:\\BIMRecommed\\input\\data_test.csv";
    private static PrepareData prepareData = null;
    private PrepareData() {
        env = ExecutionEnvironment.getExecutionEnvironment();
        readTrainPosition();
        readTestPosition();
        readTrainDirection();
        readTestDirection();
    }

    public static PrepareData getInstance() {
        if(prepareData == null) {
            prepareData = new PrepareData();
        } else {
            return prepareData;
        }
        return prepareData;
    }

    public void readTrainPosition() {
        DataSet<Position> trainPositionDataSet = env.readCsvFile(trainFilePath)
                .includeFields("1111000000")
                .pojoType(Position.class, "dataId", "px", "py", "pz");
        try {
            this.trainPosition = trainPositionDataSet.collect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readTrainDirection() {
        DataSet<Direction> trainDirectionDataSet = env.readCsvFile(trainFilePath)
                .includeFields("1000111000")
                .pojoType(Direction.class, "dataId", "dx", "dy", "dz");
        try {
            this.trainDirection = trainDirectionDataSet.collect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readTestPosition() {
        this.testPositionDS = env.readCsvFile(testFilePath)
                .includeFields("1111000000")
                .pojoType(Position.class, "dataId", "px", "py", "pz");
    }

    public void readTestDirection() {
        this.testDirectionDS = env.readCsvFile(testFilePath)
                .includeFields("1000111000")
                .pojoType(Direction.class, "dataId", "dx", "dy", "dz");
    }

    public ExecutionEnvironment getEnv() {
        return env;
    }

    public List<Position> getTrainPosition() {
        return trainPosition;
    }

    public DataSet<Position> getTestPositionDS() {
        return testPositionDS;
    }

    public List<Direction> getTrainDirection() {
        return trainDirection;
    }

    public DataSet<Direction> getTestDirectionDS() {
        return testDirectionDS;
    }

    public String getTrainFilePath() {
        return trainFilePath;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public void setTrainFilePath(String filePath) {
        trainFilePath = filePath;
    }

    public void setTestFilePath(String filePath) {
        testFilePath = filePath;
    }
}
