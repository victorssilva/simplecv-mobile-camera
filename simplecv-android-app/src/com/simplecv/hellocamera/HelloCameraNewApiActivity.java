package com.simplecv.hellocamera;

import java.io.File;
import java.io.IOException;

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
	protected Uri pictureUri;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        capturedImage = (ImageView) findViewById(R.id.capturedimage);
    }
   
    public void takePicture(View view){
    	File file = new File(Environment.getExternalStorageDirectory(), "SimpleCV.jpg");
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	pictureUri = Uri.fromFile(file);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
    	startActivityForResult(intent, TAKE_PICTURE);
    }

    public void selectPicture(View view){
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	intent.setType("image/*");
    	startActivityForResult(Intent.createChooser(intent,"Select an image"),SELECT_PICTURE);
    }
    
    
    public void uploadPicture(View view) {
    	if (pictureIsSet) {
    		HttpClient httpclient = new DefaultHttpClient();
    		HttpPost httppost = new HttpPost(serverURL);
    		httppost.setHeader("User-Agent", "SimpleCV Mobile Camera");
    		String pathToPicture = pictureUri.getPath();
    		 
    		try {
    		  MultipartEntity entity = new MultipartEntity();
    		 
    		  entity.addPart("type", new StringBody("file"));
    		  entity.addPart("data", new FileBody(new File(pathToPicture),"image/jpeg"));
    		  httppost.setEntity(entity);
    		  
    		  HttpResponse httpResponse = httpclient.execute(httppost);
    		  
    		  HttpEntity responseEntity = httpResponse.getEntity();
    		  if(responseEntity!=null) {
    		      Log.i("Response", EntityUtils.toString(responseEntity));
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
					capturedImage.setImageURI(pictureUri);
					pictureIsSet = true;
					break;
				case SELECT_PICTURE:
					pictureUri = data.getData();
					capturedImage.setImageURI(pictureUri);
					setContentView(R.layout.main);
					pictureIsSet = true;
					break;
			}	
		}
	}
}