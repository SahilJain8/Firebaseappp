package com.example.firebase;

public class model {

    private  String imageurl;
    public  model(){

    }

    public model(String imageurl){
        this.imageurl=imageurl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
