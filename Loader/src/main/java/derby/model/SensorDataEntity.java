package derby.model;

import derby.utils.TimestampUtil;

import java.sql.Timestamp;

/**
 * Created by gczarnocki on 2017-07-08.
 */
public class SensorDataEntity {
    private Timestamp logTime;
    private float temperature;
    private float humidity;
    private float light;
    private float sound;
    private float proximity;
    private int threshold;

    public SensorDataEntity(
            Timestamp logTime,
            float temperature, float humidity,
            float light, float sound,
            float proximity, int threshold) {
        this.logTime = logTime;
        this.temperature = temperature;
        this.humidity = humidity;
        this.light = light;
        this.sound = sound;
        this.proximity = proximity;
        this.threshold = threshold;
    }

    public SensorDataEntity(String[] tokens) {
        for(int i = 0; i < tokens.length; i++) {
            if("nan".equals(tokens[i])) {
                tokens[i] = "0";
            }
        } // mogą się tutaj pojawić wartości 'nan';

        this.logTime = TimestampUtil.convert(tokens[0]);
        this.temperature = Float.parseFloat(tokens[1]);
        this.humidity = Float.parseFloat(tokens[2]);
        this.light = Float.parseFloat(tokens[3]);
        this.sound = Float.parseFloat(tokens[4]);
        this.proximity = Float.parseFloat(tokens[5]);
        this.threshold = Integer.parseInt(tokens[6]);
    }

    public Timestamp getLogTime() {
        return logTime;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getLight() {
        return light;
    }

    public float getSound() {
        return sound;
    }

    public float getProximity() {
        return proximity;
    }

    public int getThreshold() {
        return threshold;
    }
}
