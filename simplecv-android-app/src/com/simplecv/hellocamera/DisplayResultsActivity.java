package com.simplecv.hellocamera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

public class DisplayResultsActivity extends Activity {
	
	protected ImageView modifiedImage;
	protected Uri modifiedImageUri;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        Intent intent = getIntent();
        String uriAsString = intent.getStringExtra("uriAsString");
        modifiedImageUri = Uri.parse(uriAsString);
        modifiedImage = (ImageView) findViewById(R.id.modifiedimage);
        modifiedImage.setImageURI(modifiedImageUri);
    }
}