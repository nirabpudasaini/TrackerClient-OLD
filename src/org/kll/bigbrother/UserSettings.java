package org.kll.bigbrother;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UserSettings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		
	}
	
	

}
