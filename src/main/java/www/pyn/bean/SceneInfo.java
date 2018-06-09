package www.pyn.bean;

/**
 * Created by pyn on 2018/6/9.
 */
public class SceneInfo {
    public static double positionXmin = -40;
    public static double positionXmax = 160;
    public static double positionYmin = 0;
    public static double positionYmax = 30;
    public static double positionZmin = -70;
    public static double positionZmax = 130;
    public static double gridLength = 15;
    public static int xGridNumber = (int) Math.ceil((positionXmax - positionXmin) / gridLength);
    public static int yGridNumber = (int) Math.ceil((positionYmax - positionYmin) / gridLength);
    public static int zGridNumber = (int) Math.ceil((positionZmax - positionZmin) / gridLength);
    public static int ToGridX(double x) {
        return (int)Math.floor((x + 40) / gridLength);
    }
    public static int ToGridY(double y) {
        return (int)Math.floor(y / gridLength);
    }
    public static int ToGridZ(double z) {
        return (int)Math.floor((z + 70) / gridLength);
    }
}
