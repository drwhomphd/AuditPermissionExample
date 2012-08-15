package com.example.auditpermissionexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void btnGetID_onClick(View view) {
    	Process getIds = null;
    	
    	try {
    		getIds = new ProcessBuilder()
    					.command("id")
    					.redirectErrorStream(true)
    					.start();
    		
    		BufferedReader in = new BufferedReader(new InputStreamReader(getIds.getInputStream()));
   		 
    		String line = "";
    		
    		while((line = in.readLine()) != null) {
    			TextView txtOutput = (TextView) findViewById(R.id.txtViewOutput);
    			txtOutput.setText(line + "\n");
    		}
    		
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    		getIds.destroy();
    	}    
    }
    
    public void toggleBtnAuditStream_onClick(View view) {
    	
    }
}
