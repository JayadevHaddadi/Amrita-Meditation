package edu.amrita.elearn.iamhelper.model;

public class IamPart {
    private String name;
    private String desc;
    private int changedToTime;
    private int originalTime;
    private int audioID;

    public IamPart(String name, String desc, int timeInSec, int rawAudioID) {
        this.audioID = rawAudioID;
        setAll(name, desc, timeInSec);
    }

    private void setAll(String name, String desc, int timeInSec) {
        this.name = name;
        this.desc = desc;
        this.changedToTime = timeInSec;
        this.originalTime = timeInSec;
    }

    public String getName() {
        return name;
    }

    public int getTime() {
        return changedToTime;
    }

    void addTime(int add){
        changedToTime = originalTime + add;
    }

    public int getMaxTime() {
        return changedToTime;
    }

    public String getDesciption() {
        return desc;
    }

    public int getAudioID() {
        return audioID;
    }
}