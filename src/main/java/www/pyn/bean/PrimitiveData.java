package www.pyn.bean;

import java.io.Serializable;

public class PrimitiveData implements Serializable {
    int dataId;
    double px;
    double py;
    double pz;
    double dx;
    double dy;
    double dz;

    public PrimitiveData() {};
    public PrimitiveData(int dataId, double px, double py, double pz, double dx, double dy, double dz) {
        this.dataId = dataId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.px = px;
        this.py = py;
        this.pz = pz;
    }

    public int getDataId() {
        return this.dataId;
    }

    public Position getPosition() {
        return new Position(dataId, px, py, pz);
    }

    public Direction getDirection() {
        return new Direction(dataId, dx, dy, dz);
    }

    public double getPx() {
        return px;
    }

    public double getPy() {
        return py;
    }

    public double getPz() {
        return pz;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public double getDz() {
        return dz;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public void setPx(double px) {
        this.px = px;
    }

    public void setPy(double py) {
        this.py = py;
    }

    public void setPz(double pz) {
        this.pz = pz;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setDz(double dz) {
        this.dz = dz;
    }
}
