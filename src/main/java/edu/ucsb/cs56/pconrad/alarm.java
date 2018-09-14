package edu.ucsb.cs56.pconrad;
import com.google.gson.Gson;
import java.lang.Math;

import javax.swing.text.html.HTMLDocument.BlockElement;


class alarm{
    private String time;
    private String date;
    private String purpose;

    public alarm(){
        this.time = "";
        this.purpose = "";
        this.date = "";
    }

    public alarm(String date, String time, String purpose){
        this.time = time;
        this.purpose = purpose;
        this.date = date;
    }

    public String getDate(){
        return this.date;
    }

    public String getTime(){
        return this.time;
    }

    public String getPurpose(){
        return this.purpose;
    }

    @Override
    public String toString(){
        return "Date: "+ date + " Time: " + time + "\nPurpose: " + purpose + "\n";
    }

    @Override
    public boolean equals(Object o){
        if (o == null)
			return false;
		if (!(o instanceof alarm))
            return false;
        alarm a = (alarm) o;
        return time == a.time && purpose == a.purpose && date == a.date;
    }

    @Override
    public int hashCode() {
        return Math.abs(toString().hashCode());
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static alarm toClass(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, alarm.class);
    }
}