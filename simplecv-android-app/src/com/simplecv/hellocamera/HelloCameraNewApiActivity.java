package com.simplecv.hellocamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

public class HelloCameraNewApiActivity extends Activity {

	private static final int TAKE_PICTURE = 0;
	private static final int SELECT_PICTURE = 1;
	private boolean pictureIsSet = false;

	//private static String uploadURL = "http://10.0.2.2:8000/upload";
	//private static String modifyURL = "http://10.0.2.2:8000/process";
	private static String uploadURL = "http://mobiletest.simplecv.org:8000/upload";
	private static String modifyURL = "http://mobiletest.simplecv.org:8000/process";

	private ImageView capturedImage;
	private Uri pictureUri;
	private Bitmap pictureBitmap;
	private String pathToPicture;

	private String transformation = null;
	private String linkToOriginal = null;
	private int rotation;

	/* Possible transformations on spinner */
	private static final int INVERT = 0;
	private static final int GRAB_EDGES = 1;
	private static final int DIVIDE = 2;
	private static final int DILATE = 3;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        capturedImage = (ImageView) findViewById(R.id.capturedimage);

        Spinner spinner = (Spinner) findViewById(R.id.transformations_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.transformations_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new onTransformationSelectedListener());
    }

    public File getNewPictureFile(){
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SimpleCV"); //WHERETO? Context.DIRECTORY_PICTURES?
		if (!mediaStorageDir.exists()){
			if (!mediaStorageDir.mkdirs()){
				Log.i("SimpleCV", "Failed to create directory");
		    	mediaStorageDir = Environment.getExternalStorageDirectory();
	        }
	    }

        String timeStamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
        File file = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    	return file;
    }

    public Uri getUriFromBitmap(Bitmap bitmap) {
		File newPictureFile = getNewPictureFile();
		try {
	        FileOutputStream outStream = new FileOutputStream(newPictureFile);
	        bitmap.compress(Bitmap.CompressFormat.JPEG,100, outStream);
	        outStream.flush();
	        outStream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
        return Uri.fromFile(newPictureFile);
    }
    
    public Bitmap decodeFile(File f){
        Bitmap b = null;
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > 300 || o.outWidth > 300) { //Max size = ?
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
    
    public void refreshImage() {
    	capturedImage.setImageBitmap(pictureBitmap);
    }
    
    public void displayPicture(){
    	pictureBitmap = decodeFile(new File(pathToPicture));
    	forcePortrait();
    	if (rotation == 0) refreshImage();
    }

	public void takePicture(View view){

    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	pictureUri = Uri.fromFile(getNewPictureFile());
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
    
    public void processPicture(View view) {
		if (pictureIsSet == true && transformation != null) {
			new UploadImageTask(this).execute();
		}	
    }
    
    private class UploadImageTask extends AsyncTask<Void, String, Void> {
    	
    	private Context context;
    	ProgressDialog progressDialog;
		    
    	public UploadImageTask(Context cxt) {
            context = cxt;
            progressDialog = new ProgressDialog(context);
        }
    	
    	
    	protected void onPreExecute() {
    		progressDialog.setMessage("Preparing picture...");
    		progressDialog.show();
        }	
    	
        protected Void doInBackground(Void... unused) {
        	
    		if (linkToOriginal == null) {
	        	this.publishProgress("Uploading picture...");
    			uploadPicture();
    		}
    		
        	this.publishProgress("Getting picture back...");
			modifyPicture();
    			
			return (null);
        }
        
        protected void onPostExecute(Void unused) {
        	progressDialog.dismiss();
        }
        
        protected void onProgressUpdate(String... message) {
    		progressDialog.setMessage(message[0]);
    		progressDialog.show();
        }
    }
    
    public void uploadPicture() {

		
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(uploadURL);
			httpPost.setHeader("User-Agent", "SimpleCV Mobile Camera");

			try {
				MultipartEntity entity = new MultipartEntity();

				entity.addPart("type", new StringBody("file"));
				entity.addPart("data", new FileBody(new File(pathToPicture),"image/jpeg"));
				httpPost.setEntity(entity);

				HttpResponse httpResponse = httpclient.execute(httpPost);

				HttpEntity responseEntity = httpResponse.getEntity();
				if(responseEntity!=null) {
					linkToOriginal = EntityUtils.toString(responseEntity);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

    }


    public void modifyPicture() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(modifyURL);
		httpPost.setHeader("User-Agent", "SimpleCV Mobile Camera");

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("picture", linkToOriginal));
	        nameValuePairs.add(new BasicNameValuePair("transformation", transformation));
	        nameValuePairs.add(new BasicNameValuePair("rotation", Integer.toString(rotation)));
	        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse httpResponse = httpclient.execute(httpPost);

			HttpEntity responseEntity = httpResponse.getEntity();
			if(responseEntity!=null) {
				String transformedImageURL = EntityUtils.toString(responseEntity);
				try {
					Bitmap transformedImageBitmap = BitmapFactory.decodeStream((InputStream)new URL(transformedImageURL).getContent());
					Uri transformedImageUri = getUriFromBitmap(transformedImageBitmap);
					Intent displayIntent = new Intent(getApplicationContext(), DisplayResultsActivity.class);
					displayIntent.putExtra("uriAsString", transformedImageUri.toString());
					startActivity(displayIntent);
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


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK)
		{
			switch (requestCode){
				case TAKE_PICTURE:
					pathToPicture = pictureUri.getPath();
					break;
				case SELECT_PICTURE:
					pictureUri = data.getData();
					pathToPicture = getPathFromGallery(pictureUri);
					break;
			}
			rotation = 0;
			displayPicture();
			pictureIsSet = true;
			linkToOriginal = null;
		}
	}

	public class onTransformationSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	switch (pos){
	    		case INVERT:
	    			transformation = "invert";
	    		break;
	    		case GRAB_EDGES:
	    			transformation = "edges";
	    		break;
	    		case DIVIDE:
	    			transformation = "divide";
	    		break;
	    		case DILATE:
	    			transformation = "dilate";
	    		break;
	    	}
	    }

	    public void onNothingSelected(AdapterView<?> parent) {

	    }
	}
	
	 public void forcePortrait(){
		 if (pictureBitmap.getWidth() > pictureBitmap.getHeight()) {
			 rotate(90);
		 }
	 }
	 
	 public void rotate(int angle){
		 Matrix matrix = new Matrix();
		 rotation += angle;
	     matrix.preRotate(angle);
		 pictureBitmap = Bitmap.createBitmap(pictureBitmap, 0, 0, 
		                               pictureBitmap.getWidth(), pictureBitmap.getHeight(), 
		                               matrix, true);
		 
		 linkToOriginal = null;
		 refreshImage();
	 }

	 public void rotateCw(View view){
		 rotate(90);
	 }
	 
	 public void rotateCcw(View view){
		 rotate(-90);
	 }
}
