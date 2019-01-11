package com.example.admin.wrms_weatherstation;

public class WeatherSample {

    private String date;
    private String rainfall;
    private String sumHours;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRainfall() {
        return rainfall;
    }

    public void setRainfall(String rainfall) {
        this.rainfall = rainfall;
    }

    public String getSumHours() {
        return sumHours;
    }

    public void setSumHours(String sumHours) {
        this.sumHours = sumHours;
    }

    @Override
    public String toString() {
        return "WeatherSample{" +
                "month='" + date + '\'' +
                ", rainfall=" + rainfall +
                ", sumHours=" + sumHours +
                '}';
    }

}
