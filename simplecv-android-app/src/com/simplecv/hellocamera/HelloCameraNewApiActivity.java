package com.simplecv.hellocamera;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class HelloCameraNewApiActivity extends Activity {
	
	private static final int TAKE_PICTURE = 0;
	private static final int SELECT_PICTURE = 1;
	private boolean pictureIsSet = false;
	
	private static String serverURL = "http://10.0.2.2:8000/upload";
	
	protected ImageView capturedImage;
	protected ImageView transformedImage;
	protected Uri pictureUri;
	protected String pathToPicture;
	
	private String transformation = "edges";

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        capturedImage = (ImageView) findViewById(R.id.capturedimage);
    }
   
    public Uri getNewPictureFileUri(){
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SimpleCV");
		if (!mediaStorageDir.exists()){
			if (!mediaStorageDir.mkdirs()){
				Log.i("!", "Failed to create directory");
		    	mediaStorageDir = Environment.getExternalStorageDirectory();
	        }
	    }
		
        String timeStamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
        File file = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");		
    	return Uri.fromFile(file);
    }
    
	public void takePicture(View view){

    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	pictureUri = getNewPictureFileUri();
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
    	startActivityForResult(intent, TAKE_PICTURE);
    }

    public void selectPicture(View view){
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	intent.setType("image/*");
    	startActivityForResult(Intent.createChooser(intent,"Select an image"),SELECT_PICTURE);
    }
    
    
    public String getPathFromGallery(Uri uri) {
    	Cursor cursor = getContentResolver().query(uri, null, null, null, null); 
        cursor.moveToFirst(); 
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
        return cursor.getString(idx); 
    }
    
    public void uploadPicture(View view) {
    	if (pictureIsSet) {
    		HttpClient httpclient = new DefaultHttpClient();
    		HttpPost httppost = new HttpPost(serverURL);
    		httppost.setHeader("User-Agent", "SimpleCV Mobile Camera");
    		httppost.setHeader("Transformation", transformation);
    		
    		 
    		try {
    		  MultipartEntity entity = new MultipartEntity();
    		 
    		  entity.addPart("type", new StringBody("file"));
    		  entity.addPart("data", new FileBody(new File(pathToPicture),"image/jpeg"));
    		  httppost.setEntity(entity);
    		  
    		  HttpResponse httpResponse = httpclient.execute(httppost);
    		  
    		  HttpEntity responseEntity = httpResponse.getEntity();
    		  if(responseEntity!=null) {
    			  String transformedImageURL = EntityUtils.toString(responseEntity);
    		      try {
    		    	  Bitmap transformedImageBitmap = BitmapFactory.decodeStream((InputStream)new URL(transformedImageURL).getContent());
    		    	  capturedImage.setImageBitmap(transformedImageBitmap); 
    		    	} catch (MalformedURLException e) {
    		    	  e.printStackTrace();
    		    	} catch (IOException e) {
    		    	  e.printStackTrace();
    		    	}
    		  }
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK)
		{
			switch (requestCode){
				case TAKE_PICTURE:
					pathToPicture = pictureUri.getPath();
					capturedImage.setImageURI(pictureUri);
					pictureIsSet = true;
					break;
				case SELECT_PICTURE:
					pictureUri = data.getData();
					pathToPicture = getPathFromGallery(pictureUri);
					capturedImage.setImageURI(pictureUri);
					pictureIsSet = true;
					break;
			}	
		}
	}
}