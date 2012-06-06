package com.simplecv.hellocamera;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
   
    public void deletePicture(View view){
    	File picture = new File(modifiedImageUri.getPath());
    	if (picture.delete()) {
    		finish();
    	}
    }
    
    public void sharePicture(View view){
    	Intent sharingIntent = new Intent(Intent.ACTION_SEND);
    	sharingIntent.setType("image/png"); //png?
    	sharingIntent.putExtra(Intent.EXTRA_STREAM, modifiedImageUri);
    	startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }
}