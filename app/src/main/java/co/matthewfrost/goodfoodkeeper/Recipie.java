package co.matthewfrost.goodfoodkeeper;

/**
 * Created by matth on 22/10/2016.
 */

public class Recipie {
    String recipename;
    String addr;

    public Recipie(String name, String url){
        this.recipename = name;
        this.addr = url;
    }

    @Override
    public String toString(){
        return recipename;
    }

    public String getUrl(){
        return addr;
    }

}
