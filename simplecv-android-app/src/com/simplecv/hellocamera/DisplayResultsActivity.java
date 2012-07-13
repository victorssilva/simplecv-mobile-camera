package com.simplecv.hellocamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class DisplayResultsActivity extends Activity {
	
	protected ImageView modifiedImage;
	protected Uri modifiedImageUri;
    protected Bitmap modifiedImageBitmap;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        Intent intent = getIntent();
        String uriAsString = intent.getStringExtra("uriAsString");
        modifiedImageUri = Uri.parse(uriAsString);
        modifiedImage = (ImageView) findViewById(R.id.modifiedimage);
		new displayImageTask().execute(modifiedImageUri);
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
   
	 private class displayImageTask extends AsyncTask<Uri, Void, Void> {
		 	
		 	Bitmap bitmapToDisplay = null;
		 
			public Bitmap decodeFile(File f){
			    Bitmap b = null;
			    try {
			        BitmapFactory.Options o = new BitmapFactory.Options();
			        o.inJustDecodeBounds = true;
			
			        FileInputStream fis = new FileInputStream(f);
			        BitmapFactory.decodeStream(fis, null, o);
			        fis.close();
			
			        int scale = 1;
			        if (o.outHeight > 350 || o.outWidth > 350) { //Max size = ?
			            scale = (int)Math.pow(2, (int) Math.round(Math.log(300 / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			        }
			
			        BitmapFactory.Options o2 = new BitmapFactory.Options();
			        o2.inSampleSize = scale;
			        fis = new FileInputStream(f);
			        b = BitmapFactory.decodeStream(fis, null, o2);
			        fis.close();
			    } catch (IOException e) {
			    	e.printStackTrace();
			    }
			    return b;
			}
	    	
	        protected Void doInBackground(Uri... pictureUri) {
	          	bitmapToDisplay = decodeFile(new File(pictureUri[0].getPath()));
				return (null);
	        }
	        
	        protected void onPostExecute(Void unused) {
	        	modifiedImageBitmap = bitmapToDisplay;
		    	modifiedImage.setImageBitmap(modifiedImageBitmap);
	        }
	    }
}