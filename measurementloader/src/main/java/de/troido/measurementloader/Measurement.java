package de.troido.measurementloader;

public class Measurement {
    private double temperature = -1000;
    private boolean button1 = false;
    private boolean button2 = false;
    private int illumination = -1;
    private int potentiometer = -1000;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public boolean isButton1() {
        return button1;
    }

    public void setButton1(boolean button1) {
        this.button1 = button1;
    }

    public boolean isButton2() {
        return button2;
    }

    public void setButton2(boolean button2) {
        this.button2 = button2;
    }

    public int getIllumination() {
        return illumination;
    }

    public void setIllumination(int illumination) {
        this.illumination = illumination;
    }

    public int getPotentiometer() {
        return potentiometer;
    }

    public void setPotentiometer(int potentiometer) {
        this.potentiometer = potentiometer;
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "temperature=" + temperature +
                ", button1=" + button1 +
                ", button2=" + button2 +
                ", illumination=" + illumination +
                ", potentiometer=" + potentiometer +
                '}';
    }
}
