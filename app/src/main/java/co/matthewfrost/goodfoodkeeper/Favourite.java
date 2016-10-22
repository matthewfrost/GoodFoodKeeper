package co.matthewfrost.goodfoodkeeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class Favourite extends AppCompatActivity {

    ListView fileView;
    ArrayAdapter<String> fileAdapter;
    ArrayList<String> files;
    ArrayList<File> absFiles;
    File[] Dirfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        files = new ArrayList<>();
        absFiles = new ArrayList<>();
        getFiles();
        fileView = (ListView) findViewById(R.id.files);
        fileAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, files);
        fileView.setAdapter(fileAdapter);

        fileView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fPath = absFiles.get(position).getAbsolutePath();
                Intent i = new Intent(getApplicationContext(), Details.class);
                i.putExtra("filePath", fPath);
                startActivity(i);
            }
        });

    }

    public void getFiles(){
        String path;
        File dir;

        path = "/data/data/" + getApplicationContext().getPackageName() + "/";
        dir = new File(path);
        Dirfiles = dir.listFiles();
        for(int i = 0; i < Dirfiles.length; i++){
            if(!Dirfiles[i].isDirectory()){
                String fileName = Dirfiles[i].getName();
                files.add(fileName.substring(0, fileName.indexOf('.')));
                absFiles.add(Dirfiles[i]);
            }
        }
    }
}
