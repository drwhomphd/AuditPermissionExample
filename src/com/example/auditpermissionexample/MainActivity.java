package com.example.auditpermissionexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Queue;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.os.*;

public class MainActivity extends Activity {

	// Thread for the audit stream.
	private Thread auditstream;
	
	private void auditctlInit() {
		
		try {
			// Reload the rules file. This will (usually) contain a -D at the beginning for a clean slate. Done so that
			// we don't add a bunch of different pids every time the application starts as the pid will change each run.
			Runtime.getRuntime().exec("auditctl -R /etc/audit/auditd.rules"); 
			
			// Ignore our pid
			Runtime.getRuntime().exec("auditctl -A exit,never -F pid=" + android.os.Process.myPid()); // Ignore our own PID
		} catch (IOException e) {
    		e.printStackTrace();
    		//auditctl.destroy();
    	}
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        auditctlInit();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    public void btnListRules_onClick(View view) {
    	try {
			
    		String line;
    		
    		// List our current rules
    		java.lang.Process auditctl = Runtime.getRuntime().exec("auditctl -l");
			
			// Get our input stream
    		BufferedReader in = new BufferedReader(new InputStreamReader(auditctl.getInputStream()));
    		
    		while((line = in.readLine()) != null) {
    			TextView txtOutput = (TextView) findViewById(R.id.txtViewOutput);
    			txtOutput.append(line + "\n");
    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public void btnGetID_onClick(View view) {
    	java.lang.Process getIds = null;
    
    	// Create our ID process, run it, and print the output.
    	try {
    		getIds = new ProcessBuilder()
    					.command("id")
    					.redirectErrorStream(true)
    					.start();
    		
    		BufferedReader in = new BufferedReader(new InputStreamReader(getIds.getInputStream()));
   		 
    		String line = "";
    		
    		while((line = in.readLine()) != null) {
    			TextView txtOutput = (TextView) findViewById(R.id.txtViewOutput);
    			txtOutput.append(line + "\n");
    		}
    		
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    		getIds.destroy();
    	}    
    }
    
    public void toggleBtnAuditStream_onClick(View view) {
    	// Check our button state.
    	ToggleButton toggleBtnAudit = ((ToggleButton) view);
    	
    	//The click turned the button on
    	if(toggleBtnAudit.isChecked()) {
    		
    		// Kill the thread if it already exists
    		if(auditstream != null)
    			auditstream.interrupt();
    		
    		// Start our thread
    		auditstream = new Thread(new AuditStreamReader(view));
    		auditstream.start();
    	}
    	else { // The click turned the button off
    		if(auditstream != null)
    			auditstream.interrupt();
    	}
    	
    	
    }
    
    /** An extra thread that deals with connecting to and reading the audit stream without
     * interrupting the original UI thread.
     */
    private class AuditStreamReader implements Runnable {
    	
    	private Queue<String> audit_record;
    	private static final String AUDIT_DEVICE = "/dev/audit";
    	private LocalSocket auditStream;
    	private View parent_view; // The view of this thread's parent UI
    	
    	/** 
    	 * 
    	 * @param view The view of the object that started the thread. Used for updating the UI thread.
    	 */
    	public AuditStreamReader(View view) {
    		this.parent_view = view;
    	}
    	
				@Override
		public void run() {
    		// Set up the local socket address to our audit stream
	    	LocalSocketAddress auditAddress = new LocalSocketAddress(AUDIT_DEVICE, LocalSocketAddress.Namespace.FILESYSTEM);
	    	
	    	// Open audit stream device with Local Socket
	    	auditStream = new LocalSocket();

			BufferedReader in;
	    	
	    	try {
				auditStream.connect(auditAddress);
				
				in = new BufferedReader(new InputStreamReader(auditStream.getInputStream()));

				try {
					// Read until we're interrupted
					while(!Thread.currentThread().isInterrupted()) {
						// Read line
						final String line = in.readLine();

						if(line != null) {
							parent_view.post(new Runnable() {
								public void run() {
									TextView txtOutput = (TextView) findViewById(R.id.txtViewOutput);		
									txtOutput.append(line + "\n");
								}
							});
						}

						Thread.sleep(100);
					}

				}catch (InterruptedException e) {
					android.util.Log.i("threads", "Audit Stream Thread Interrupted, closing stream.");
					auditStream.close();
					return;
				}
				
				// Loop is done, we were interrupted, close stream
				android.util.Log.i("threads", "Audit Stream Thread Interrupted, closing stream.");
				auditStream.close();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
    	
    }
}
