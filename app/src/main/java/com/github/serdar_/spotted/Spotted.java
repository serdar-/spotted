package com.github.serdar_.spotted;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Spotted extends ActionBarActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    String CurrentPhotoPath = null;
    String absImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotted);
        final String fileUploadSever = "http://example.com/upload.php";
        // Picture taking button
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                dispatchTakePictureIntent();
            }
        });
        // Image upload button
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                uploadPicture(fileUploadSever);
            }
        });
    }

    private File createImageFile() throws IOException{

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Spotted_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        absImagePath = image.getAbsolutePath();
        CurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;

    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Context context = getApplicationContext();
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            // Create a file for picture to be taken
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException ex){
                CharSequence text = "Error: Image file was not created!";
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
            if(photoFile != null){
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if( requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Context context = getApplicationContext();
            CharSequence text = "Image was saved.";
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

        }
    }

    private void uploadPicture(String uploadServer){

        if(absImagePath != null){
            HttpURLConnection con = null;
            DataOutputStream dos = null;
            byte[] buffer;
            File upFile = new File(absImagePath);
            try{
                FileInputStream fis = new FileInputStream(upFile);
                URL url = new URL(uploadServer);
                con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");
                con.setRequestProperty("Connection", "Keep-Alive");
                con.setRequestProperty("ENCTYPE", "multipart/form-data");
                con.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
                con.setRequestProperty("uploaded_file", absImagePath);
                dos = new DataOutputStream(con.getOutputStream());
                dos.writeBytes("--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=" + absImagePath + "\r\n");
                dos.writeBytes("\r\n");
                int availableBytes = fis.available();
                int bufferSize = Math.min(availableBytes, 1024*1024);
                buffer = new byte[bufferSize];
                int bytesRead = fis.read(buffer, 0, bufferSize);
                while (bytesRead > 0){
                    dos.write(buffer, 0 , bufferSize);
                    availableBytes = fis.available();
                    bufferSize = Math.min(availableBytes,1024*1024);
                    bytesRead = fis.read(buffer, 0, bufferSize);
                }
                dos.writeBytes("\r\n");
                dos.writeBytes("--*****--\r\n");
                if(con.getResponseCode() == 200){
                    runOnUiThread(new Runnable(){
                        public void run(){
                            Context context = getApplicationContext();
                            CharSequence message = "Picture is uploaded to server.";
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                fis.close(); // close input stream
                dos.flush(); // close output stream
                dos.close();
            } catch (Exception e){
                runOnUiThread(new Runnable(){
                    public void run() {
                        Context context = getApplicationContext();
                        CharSequence message = "Error occurred during upload...";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotted, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
