package ping.otmsapp.entitys.map;

/**
 * Created by Leeping on 2018/3/28.
 * email: 793065165@qq.com
 * 轨迹记录使用
 */

public class MTraceLocation {

    private double latitude;
    private double longitude;
    private float speed;
    private float bearing;
    private long time;


    public MTraceLocation(double latitude, double longitude, float speed, float bearing, long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.bearing = bearing;
        this.time = time;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", time=" + time +
                '}';
    }
}
