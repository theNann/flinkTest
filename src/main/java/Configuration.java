public class Configuration {
    private String ip;
    private int readPort;
    private int writePort;
    private String trainDataPath;
    private String testDataPath;
    private String trainTargetPath;
    private String testTargetPath;
    private static Configuration instance = null;

    private Configuration() {

    }

    public static Configuration getInstance() {
        if(instance == null) {
            instance = new Configuration();
        } else {
            return instance;
        }
        return instance;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getReadPort() {
        return readPort;
    }

    public void setReadPort(int readPort) {
        this.readPort = readPort;
    }

    public int getWritePort() {
        return writePort;
    }

    public void setWritePort(int writePort) {
        this.writePort = writePort;
    }

    public String getTrainDataPath() {
        return trainDataPath;
    }

    public void setTrainDataPath(String trainDataPath) {
        this.trainDataPath = trainDataPath;
    }

    public String getTestDataPath() {
        return testDataPath;
    }

    public void setTestDataPath(String testDataPath) {
        this.testDataPath = testDataPath;
    }

    public String getTrainTargetPath() {
        return trainTargetPath;
    }

    public void setTrainTargetPath(String trainTargetPath) {
        this.trainTargetPath = trainTargetPath;
    }

    public String getTestTargetPath() {
        return testTargetPath;
    }

    public void setTestTargetPath(String testTargetPath) {
        this.testTargetPath = testTargetPath;
    }
}
