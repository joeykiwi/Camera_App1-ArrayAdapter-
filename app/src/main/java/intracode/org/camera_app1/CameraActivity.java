package intracode.org.camera_app1;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class CameraActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    int state = 0;
    String sdCardDir = Environment.getExternalStorageDirectory().toString() + "/DCIM/Photo";

    private int count;
    private Bitmap[] thumbNails;
    private boolean[] thumbNailsSelection;
    private String[] arrPath;
    private ImageAdapter imageAdapter;
    ArrayList<String> fileArray = new ArrayList<String>();
    File[] files;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParams);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView) findViewById(R.id.snap_Frame);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(surfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        Button cameraButton = (Button) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.startPreview();
            }
        });

        Button snapButton = (Button) findViewById(R.id.snap_button);
        snapButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                camera.takePicture(sCallback, rawCallback, jpgCallback);
            }

        });

        Button albumButton = (Button) findViewById(R.id.album_button);
        albumButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {


//                setContentView(R.layout.listview);
                getPhotos();
                GridView gridView = (GridView) findViewById(R.id.grid_view);
                imageAdapter = new ImageAdapter();
                gridView.setAdapter(imageAdapter);

            }
        });




    }

    public void getPhotos() {

        int count = 0;
        File file = new File(sdCardDir, "tempImages");
        if (file.isDirectory()) {
            files = file.listFiles();
            for (int i = 0; i < files.length; i++, count++) {
                fileArray.add(files[i].getAbsolutePath());
            }
            Log.d("Joey", "there are " + count + " photos");

        }
    }

    PictureCallback jpgCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            camera.stopPreview();

            Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);

            Bitmap finalBitmap = rotatePortrait(bitmapPicture);


            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File takenImage = new File(sdCardDir, timeStamp + "_Image.jpeg");

            FileOutputStream outStream;
            try {
                outStream = new FileOutputStream(takenImage);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();

            } catch (FileNotFoundException fe) {
                fe.printStackTrace();
            } catch (IOException ie) {
                ie.printStackTrace();
            }

            Toast.makeText(getApplicationContext(), "The photo will save as " + takenImage.toString(), Toast.LENGTH_LONG).show();

        }
    };

    private Bitmap rotatePortrait(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(90);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    ShutterCallback sCallback = new ShutterCallback() {
        @Override
        public void onShutter() {
            //do nothing yet
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //do nothing yet
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = camera.open();
        camera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }


    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        public ImageAdapter() {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return fileArray.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.imageview, null);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Bitmap bitmapImages = BitmapFactory.decodeFile(fileArray.get(position));
            holder.imageView.setImageBitmap(bitmapImages);

            return convertView;
        }
    }
    class ViewHolder {
        ImageView imageView;
    }

}