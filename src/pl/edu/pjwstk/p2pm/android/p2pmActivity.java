package pl.edu.pjwstk.p2pm.android;

import pl.edu.pjwstk.p2pp.launchers.CommandLineLauncher;
import android.app.Activity;
import android.os.Bundle;

public class p2pmActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CommandLineLauncher.main(null);
	}
}
