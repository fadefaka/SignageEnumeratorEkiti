package com.biscom.signageenumeratorekiti;
import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.kosalgeek.android.imagebase64encoder.ImageBase64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Timer;

import android.app.AlertDialog.Builder;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AfterLoginActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // LogCat tag
    private static final String TAG = "AfterLogin";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 0; // 10 meters

    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private static String responseJSON;
    ListView lstStructures;
    private static String SearchS;
    private Timer timer;
    private static String neededName;
    private static String neededLocation;
    private static String neededStructureType;
    private static String neededCurrentBilledYear;
    private static String neededNegotiatedAmount;
    private static String neededAmountPaid;
    private static String neededLongitude;
    private static String neededLatitude;
    private static String neededImageName;
    private static String neededStructureID;
    Integer sItemPosition;
    private JSONArray arrayresultformyStructures;
    private ArrayList<String> myStructurearraylist;
    ProgressBar pg;
    private AnimationDrawable animation;
    Button btnfind;
    Button btnback;
    Button btnlogout;
    EditText txtfind;

    static final Integer LOCATION = 0x1;
    //static final Integer CALL = 0x2;
    static final Integer WRITE_EXST = 0x3;
    //static final Integer READ_EXST = 0x4;
    static final Integer CAMERA = 0x5;
    //static final Integer ACCOUNTS = 0x6;
    //static final Integer GPS_SETTINGS = 0x7;
    ProgressDialog progressDialog;
    SQLiteDatabase db;
    String exprefno;
    String expvaluedate;
    String expStructureID;
    String[] reflistc1;
    String[] reflistc2;
    String[] reflistc3;
    int reflistcounter=0;

    double latitude;
    double longitude;
    String User_Fk="0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);
        myStructurearraylist = new ArrayList<String>();
        btnfind = (Button) findViewById(R.id.btnsearch);
        btnback = (Button) findViewById(R.id.btnback);
        btnlogout = (Button) findViewById(R.id.btnlogout);
        txtfind = (EditText) findViewById(R.id.txtsearchtext);
        lstStructures = (ListView) findViewById(R.id.lstResult);
        pg = (ProgressBar) findViewById(R.id.progressBar1);

        //Check for Previous Login
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
       String shareduser = sharedPref.getString("PREUSERNAME","");
        User_Fk = sharedPref.getString("User_Fk","0");
        //Show Welcom Msg

        LayoutInflater inflater= LayoutInflater.from(AfterLoginActivity.this);
        View view=inflater.inflate(R.layout.alertview, null);
        TextView textview=(TextView)view.findViewById(R.id.textmsg);
        textview.setText(" Welcome back "+"\n"+shareduser);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AfterLoginActivity.this);
        alertDialog.setTitle("Home Message");
        alertDialog.setIcon(R.mipmap.ic_launcher);
        //alertDialog.setMessage("Here is a really long message.");
        alertDialog.setView(view);
        alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();

        //End Show Welcome Msg


            try{
                // First we need to check availability of play services
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                    createLocationRequest();
                }
            }catch(Exception e){

            }



        btnfind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Utils.hideKeyboard(AfterLoginActivity.this);
                try{
                    togglePeriodicLocationUpdates(); //Starting the location updates
                }catch(Exception e){

                }
                SearchS = txtfind.getText().toString();

                if (SearchS.length()>2){
                    JSON_SearchStructureFrmMobile task = new JSON_SearchStructureFrmMobile();
                    task.execute();
                }

                // Run a timer after you started the AsyncTask
                //try{
                   // new CountDownTimer(60000, 1000) {

                        //public void onTick(long millisUntilFinished) {
                            // Do nothing
                        //}
                       // public void onFinish() {
                            //task.cancel(true);
                        //}
                  //  }.start();
                //}catch(Exception e){

                //}
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent myintent = new Intent(AfterLoginActivity.this, MenuActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());
                //finish();
            }
        });

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("PREUSERNAME", "");
                editor.putString("PREPASSWORD", "");
                editor.commit();
                Intent myintent = new Intent(AfterLoginActivity.this, FirstActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());
                finish();
            }
        });

        lstStructures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sItemPosition=arg2;
                getneededvalues(sItemPosition);
                pg.setVisibility(View.GONE);
                LayoutInflater inflater= LayoutInflater.from(AfterLoginActivity.this);
                View view=inflater.inflate(R.layout.alertview, null);
                TextView textview=(TextView)view.findViewById(R.id.textmsg);
                textview.setText(Html.fromHtml("<b>Business Name:</b> ").toString()+"\n"+neededName+"\n"+"\n"+"Structure Type: "+"\n"+neededStructureType+"\n"+"\n"+"Location: "+"\n"+neededLocation+"\n"+"\n"+"Current Billing Year: "+"\n"+neededCurrentBilledYear+"\n"+"\n"+"Amount Billed: "+"\n"+neededNegotiatedAmount+"\n"+"\n"+"Amount Paid: "+"\n"+neededAmountPaid);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AfterLoginActivity.this);
                alertDialog.setTitle("Details");
                alertDialog.setIcon(R.mipmap.ic_launcher);
                //alertDialog.setMessage("Here is a really long message.");
                alertDialog.setView(view);
                alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                alertDialog.setNeutralButton("Show On Map",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        //Toast.makeText(AfterLoginActivity.this, "Hey Its Me", Toast.LENGTH_LONG).show();
                        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("MAP_Latitude", neededLatitude);
                        editor.putString("MAP_Longitude", neededLongitude);
                        editor.putString("MAP_ImageName", neededImageName);
                        editor.putString("MAP_BusinessName", neededName);
                        editor.commit();

                        Intent myintent = new Intent(AfterLoginActivity.this, MapViewActivity.class);
                        startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());
                    }
                });

                alertDialog.setNegativeButton("CAP",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        //Toast.makeText(AfterLoginActivity.this, "Hey Its Me", Toast.LENGTH_LONG).show();
                        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("neededStructureID", neededStructureID);
                        editor.commit();

                        Intent myintent = new Intent(AfterLoginActivity.this, EnforcementTakeAShotActivity.class);
                        startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();
                //Toast.makeText(MyPendingWorkActivity.this, neededProjectName.toString(), Toast.LENGTH_LONG).show();
            }
        });

        //Begin of Button Click Event for enumerator
        final Button EnumeratorButton = (Button) findViewById(R.id.btnenumerator);
        EnumeratorButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //What happens when button is clicked goes here
                //Intent myintent = new Intent(AfterLoginActivity.this, ViewRecordActivity.class);
                //startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());
                try {
                    db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
                    //db.execSQL("CREATE TABLE IF NOT EXISTS reftable(refno VARCHAR,istreated VARCHAR,valuedate VARCHAR);");
                    Cursor c=db.rawQuery("SELECT * FROM reftable WHERE istreated='NO'", null);
                    Toast.makeText(AfterLoginActivity.this, Integer.toString(c.getCount()), Toast.LENGTH_SHORT).show();
                    if(c.getCount()==0)
                    {
                        //Toast.makeText(ViewRecordActivity.this, "Some RECORDS Found", Toast.LENGTH_SHORT).show();
                        showMessage("Pending Export", "No records found");
                        return;
                    }
                    StringBuffer buffer=new StringBuffer();
                    while(c.moveToNext())
                    {
                        buffer.append(c.getString(0).replace(".jpg","")+"\n");
                        buffer.append("--------------"+"\n");
                        //buffer.append("Name: "+c.getString(1)+"\n");
                        //buffer.append("Marks: "+c.getString(2)+"\n\n");
                    }
                    showMessage("Pending Export", buffer.toString());
                } catch (Exception e) {
                    Toast.makeText(AfterLoginActivity.this, "You have to Capture First",
                            Toast.LENGTH_SHORT).show();
                }



            }
        }); //End of Button Click Event for enumerator

        //Begin of Button Click Event for btnexport
        final Button btnexport = (Button) findViewById(R.id.btnexport);
        btnexport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
                    //db.execSQL("CREATE TABLE IF NOT EXISTS reftable(refno VARCHAR,istreated VARCHAR,valuedate VARCHAR);");
                    Cursor c=db.rawQuery("SELECT * FROM reftable WHERE istreated='NO'", null);
                    //Toast.makeText(AfterLoginActivity.this, Integer.toString(c.getCount()), Toast.LENGTH_SHORT).show();

                    if(c.getCount()==0)
                    {
                        //Toast.makeText(ViewRecordActivity.this, "Some RECORDS Found", Toast.LENGTH_SHORT).show();
                        showMessage("Pending Export", "No records found");
                        return;
                    }
                    reflistc1 = new String[c.getCount()];
                    reflistc2 = new String[c.getCount()];
reflistcounter=0;
                    while(c.moveToNext())
                    {

                        exprefno=c.getString(0);
                        expvaluedate=c.getString(2);
                        reflistc1[reflistcounter]=exprefno;
                        reflistc2[reflistcounter]=expvaluedate;

                        //buffer.append(c.getString(0).replace(".jpg","")+"\n");
                        //buffer.append("--------------"+"\n");
                        //buffer.append("Name: "+c.getString(1)+"\n");
                        //buffer.append("Marks: "+c.getString(2)+"\n\n");
                        reflistcounter = reflistcounter+1;
                    }
                    //reflistcounter=0;
                    // iterate all the elements of the array
                    //int size = reflistc1.length;
                    //for (int i = 0; i < size; i++) {
                        //exprefno=reflistc1[i];
                        //expvaluedate=reflistc2[i];
                        JSON_SignageRecieveRefNoFromMobile task = new JSON_SignageRecieveRefNoFromMobile();
                        task.execute();
                   // }

/*
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AfterLoginActivity.this);
                    dialog.setTitle( "Finished" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("Done with the Export\n Check Pending Export to be sure everything went through :)")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            }).show();
                    progressDialog.cancel();*/
                } catch (Exception e) {
                    Toast.makeText(AfterLoginActivity.this, "You Have to Capture First",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }); //End of Button Click Event for btnexport

        final Button btnexportenforcements = (Button) findViewById(R.id.btnexportenforcements);
        btnexportenforcements.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
                    Cursor c=db.rawQuery("SELECT * FROM BillManifestUpdate WHERE IsPushed='0'", null);
                    if(c.getCount()==0)
                    {
                        showMessage(String.valueOf(c.getCount())+" Un-pushed Data", "All data has been Pushed");
                        return;
                    }else{
                        showMessage(String.valueOf(c.getCount())+" Un-pushed Data", "Attempting to push...\nCheck in few minutes");
                        return;
                    }
//                    reflistc1 = new String[c.getCount()];
//                    reflistc2 = new String[c.getCount()];
//                    reflistc3 = new String[c.getCount()];
//                    reflistcounter=0;
//                    while(c.moveToNext())
//                    {
//                        exprefno=c.getString(0);
//                        expvaluedate=c.getString(2);
//                        expStructureID=c.getString(3);
//                        reflistc1[reflistcounter]=exprefno;
//                        reflistc2[reflistcounter]=expvaluedate;
//                        reflistc3[reflistcounter]=expStructureID;
//                        reflistcounter = reflistcounter+1;
//                    }

                    //JSON_SignageRecieveEnforceIDFromMobile task = new JSON_SignageRecieveEnforceIDFromMobile();
                    //task.execute();

                } catch (Exception e) {
                    Toast.makeText(AfterLoginActivity.this, "No Local Database yet",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }); //End of Button Click Event for btnexportenforcements

        //Begin of Button Click Event for AnonButton (Capture Buttion)
        final Button btnstartcapture = (Button) findViewById(R.id.btnstartcapture);
        btnstartcapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //What happens when button is clicked goes here
                try{
                    //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                    askForPermission(Manifest.permission.CAMERA,CAMERA);
                    //askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
                }catch (Exception e){
                    e.printStackTrace();
                    //Intent myintent = new Intent(AfterLoginActivity.this, LoggedInTakeAShotActivity.class);
                    //startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());
                    Toast.makeText(AfterLoginActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
                }

            }
        }); //End of Button Click Event for AnonButton

    } //End of OnCreate


    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //What todo if there is no permission
            //Toast.makeText(AfterLoginActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
        }
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                //lblLocation.setText(latitude + ", " + longitude);
                //Toast.makeText(AfterLoginActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();

            } else {
                //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                //Toast.makeText(AfterLoginActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    private class JSON_SearchStructureFrmMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            //Invoke web method 'PopulateCountries' with dummy value
            //Toast.makeText(getBaseContext(), "Am here! ", Toast.LENGTH_LONG).show();
            invokeJSONWS("JSON_SearchStructureFrmMobile");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");

            JSONObject j = null;
            try {
                if (responseJSON.contains("-NOT FOUND-")){
                    //pg.setVisibility(View.GONE);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AfterLoginActivity.this);
                    dialog.setTitle( "NOT FOUND" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("Take Picture to Document it")
//  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            }).show();
                    progressDialog.cancel();
                    //RelativeLayout rl =(RelativeLayout) findViewById(R.id.animation);
                    //rl.setBackgroundResource(R.drawable.landbg);
                    j = new JSONObject(responseJSON);
                    //Storing the Array of JSON String to our JSON Array
                    arrayresultformyStructures = j.getJSONArray("myJresult");
                    getMyStructures(arrayresultformyStructures);
                    return;
                }
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultformyStructures = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                getMyStructures(arrayresultformyStructures);
                AlertDialog.Builder dialog = new AlertDialog.Builder(AfterLoginActivity.this);
                dialog.setTitle( "Search Complete" )
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Confirm if the actual name exists/ Otherwise take a picture")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                            }
                        }).show();
                //pg.setVisibility(View.GONE);
                progressDialog.cancel();
                //RelativeLayout rl =(RelativeLayout) findViewById(R.id.animation);
                //rl.setBackgroundResource(R.drawable.landbg);


                //Toast.makeText(RptLastDaysActivity.this, getmyuserTOKEN, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            //RelativeLayout rl =(RelativeLayout) findViewById(R.id.animation);
            //rl.setBackgroundResource(R.color.white);
            progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        // Set Name
        paramPI.setName("SearchString");
        // Toast.makeText(FinalAssignActivity.this, "Oh no! " + getmyuserTOKEN, Toast.LENGTH_LONG).show();
        // Set Value
        paramPI.setValue(SearchS.toString());
        //Toast.makeText(AfterLoginActivity.this, SearchS.toString(), Toast.LENGTH_LONG).show();
        // Set dataType
        paramPI.setType(String.class);
        // Add the property to request object
        request.addProperty(paramPI);
        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            // Invole web service
            androidHttpTransport.call(SOAP_ACTION+methName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to static variable
            responseJSON = response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            responseJSON="Nothing Returned";

        }
    }
    private void getMyStructures(JSONArray j){
        //Traversing through all the items in the json array
        myStructurearraylist.clear();

        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                //Adding the name of the student to array list
                myStructurearraylist.add(json.getString("StructureID").toString()+"\n" + json.getString("BusinessName").toString() +" ("+json.getString("TheStructureType").toString()+")");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, myStructurearraylist){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position %2 == 1)
                {
                    // Set a background color for ListView regular row/item
                    view.setBackgroundColor(Color.parseColor("#EAEDED"));
                }
                else
                {
                    // Set the background color for alternate row/item
                    view.setBackgroundColor(Color.parseColor("#D5DBDB"));
                }
                return view;
            }
        };
        //Setting adapter to show the items in the spinner
        //lstpendingstaff.setAdapter(new ArrayAdapter<>(MyPendingWorkActivity.this, android.R.layout.simple_list_item_1, mypendingtaskarraylist)
        lstStructures.setAdapter(arrayAdapter);
        // );
    }
    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =arrayresultformyStructures.getJSONObject(position);
            //Fetching name from that object
            neededName = json.getString("BusinessName");
            neededLocation = json.getString("StructureLocation_Town");
            neededStructureType = json.getString("TheStructureType");
            neededCurrentBilledYear = json.getString("CurrentBilledYear");
            neededNegotiatedAmount = "N "+String.format("%,.2f",Double.valueOf(json.getString("NegotiatedAmount")));
            neededAmountPaid = "N "+String.format("%,.2f",Double.valueOf(json.getString("AmountPaid")));
            neededLatitude = json.getString("Latitude");
            neededLongitude = json.getString("Longitude");
            neededImageName = json.getString("ImageName");
            neededStructureID = json.getString("StructureID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(AfterLoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "" + permission + " is not granted.", Toast.LENGTH_SHORT).show();
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AfterLoginActivity.this, permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(AfterLoginActivity.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(AfterLoginActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            //Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
            Intent myintent = new Intent(AfterLoginActivity.this, LoggedInTakeAShotActivity.class);
            startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
                switch (requestCode) {
                    //Location
                    case 1:
                        //askForGPS();
                        break;
                    //Call
                    //case 2:
                    //Intent callIntent = new Intent(Intent.ACTION_CALL);
                    //callIntent.setData(Uri.parse("tel:" + "{This is a telephone number}"));
                    //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    //     startActivity(callIntent);
                    // }
                    // break;
                    //Write external Storage
                    case 3:
                        break;
                    //Read External Storage
                    //case 4:
                    // Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // startActivityForResult(imageIntent, 11);
                    // break;
                    //Camera
                    case 5:
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, 12);
                        }
                        break;

                }

                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                Intent myintent = new Intent(AfterLoginActivity.this, LoggedInTakeAShotActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(AfterLoginActivity.this).toBundle());

            }else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();


        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch(Exception e){

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            //stopLocationUpdates();
        }catch(Exception e){

        }

    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));
            try{
                mRequestingLocationUpdates = true;

                // Starting the location updates
                startLocationUpdates();

                Log.d(TAG, "Periodic location updates started!");
            }catch(Exception e){

            }


        } else {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

            // mRequestingLocationUpdates = false;

            // Stopping the location updates
            //stopLocationUpdates();

            //Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //What todo if there is no permission
            //Toast.makeText(AfterLoginActivity.this, "(No permission on the device YET!)", Toast.LENGTH_LONG).show();
        }else{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }


    }
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        try{
            mLastLocation = location;

            if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(Cannot Save: No permission on the device)", Toast.LENGTH_LONG).show();
            }else{
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                    JSON_PushNewLocationFromMobile task = new JSON_PushNewLocationFromMobile();
                    task.execute();
                    //lblLocation.setText(latitude + ", " + longitude);
                    //Toast.makeText(LoggedInTakeAShotActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                    Toast.makeText(AfterLoginActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                    longitude=0.00;
                    latitude=0.00;
                }


            }

            //Toast.makeText(getApplicationContext(), "Location changed!",
            //Toast.LENGTH_SHORT).show();
        }catch(Exception e){

        }

        // Displaying the new location on UI
        //displayLocation();
    }


    private class JSON_SignageRecieveRefNoFromMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Log.i(TAG, "doInBackground");
            //Invoke web method 'PopulateCountries' with dummy value
            //Toast.makeText(getBaseContext(), "Am here! ", Toast.LENGTH_LONG).show();

            //for (int i = 0; i < size; i++) {
                exprefno=reflistc1[reflistcounter-1];
                expvaluedate=reflistc2[reflistcounter-1];
                invokeJSONWS2("JSON_SignageRecieveRefNoFromMobile");
            //}
            //invokeJSONWS2("JSON_SignageRecieveRefNoFromMobile");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.i(TAG, "onPostExecute");
            progressDialog.cancel();
            JSONObject j = null;
            try {

                if (responseJSON.contains("-FAILED-")){
                    progressDialog.cancel();
                } else if (responseJSON.contains("-SUCCESSFUL-")){
                    progressDialog.cancel();
                    db.execSQL("UPDATE reftable SET istreated='YES' WHERE myrefno='"+exprefno+"'");
                }else{
                    progressDialog.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            reflistcounter=reflistcounter-1;
            if (reflistcounter>0){
                JSON_SignageRecieveRefNoFromMobile task = new JSON_SignageRecieveRefNoFromMobile();
                task.execute();
            }
            if (reflistcounter==0){

                    AlertDialog.Builder dialog = new AlertDialog.Builder(AfterLoginActivity.this);
                    dialog.setTitle( "Export Finished" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("Done with the Export\n Check Pending Export to be sure everything went through :)")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            }).show();
                    progressDialog.cancel();
            }


        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS2(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("PictureRef");
        paramPI.setValue(exprefno.toString());
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("ValueDate");
        paramPI2.setValue(expvaluedate.toString());
        paramPI2.setType(String.class);
        request.addProperty(paramPI2);


        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        try {
            // Invole web service
            androidHttpTransport.call(SOAP_ACTION+methName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to static variable
            responseJSON = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            responseJSON="Nothing Returned";
        }
    }


    private class JSON_SignageRecieveEnforceIDFromMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            exprefno=reflistc1[reflistcounter-1];
            expvaluedate=reflistc2[reflistcounter-1];
            expStructureID=reflistc3[reflistcounter-1];
            invokeJSONWS4("JSON_SignageRecieveEnforceIDFromMobile");
            //}
            //invokeJSONWS2("JSON_SignageRecieveRefNoFromMobile");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.i(TAG, "onPostExecute");
            progressDialog.cancel();
            JSONObject j = null;
            try {

                if (responseJSON.contains("-FAILED-")){
                    progressDialog.cancel();
                } else if (responseJSON.contains("-SUCCESSFUL-")){
                    progressDialog.cancel();
                    db.execSQL("UPDATE enforcementtable SET istreated='YES' WHERE myrefno='"+exprefno+"'");
                }else{
                    progressDialog.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            reflistcounter=reflistcounter-1;
            if (reflistcounter>0){
                JSON_SignageRecieveEnforceIDFromMobile task = new JSON_SignageRecieveEnforceIDFromMobile();
                task.execute();
            }
            if (reflistcounter==0){

                AlertDialog.Builder dialog = new AlertDialog.Builder(AfterLoginActivity.this);
                dialog.setTitle( "Export Finished" )
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Done with the Export")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                            }
                        }).show();
                progressDialog.cancel();
            }


        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS4(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("PictureRef");
        paramPI.setValue(exprefno.toString());
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("ValueDate");
        paramPI2.setValue(expvaluedate.toString());
        paramPI2.setType(String.class);
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("StructureID");
        paramPI3.setValue(expStructureID.toString());
        paramPI3.setType(String.class);
        request.addProperty(paramPI3);

        String root = Environment.getExternalStorageDirectory().toString();
        File newDir = new File(root + "/ENFORCEMENT_Images/"+expvaluedate.toString());
        File file = new File (newDir, exprefno.toString()+".jpg");
        String encodedImage="";
        //Toast.makeText(this, file.toString(), Toast.LENGTH_LONG).show();
        encodedImage=convertToBase64(file.toString());
//        try {
//            encodedImage = ImageBase64
//                    .with(getApplicationContext())
//                    .requestSize(512, 512)
//                    .encodeFile(file.toString());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        PropertyInfo paramPI4 = new PropertyInfo();
        paramPI4.setName("ImageBase64");
        paramPI4.setValue(encodedImage.toString());
        paramPI4.setType(String.class);
        request.addProperty(paramPI4);

        PropertyInfo paramPI5 = new PropertyInfo();
        paramPI5.setName("EnforcedBy_User_FK");
        paramPI5.setValue(User_Fk.toString());
        paramPI5.setType(String.class);
        request.addProperty(paramPI5);

        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        try {
            // Invole web service
            androidHttpTransport.call(SOAP_ACTION+methName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to static variable
            responseJSON = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            responseJSON="Nothing Returned";
        }
    }
    
    public void showMessage(String title,String message)
    {
        Builder builder=new Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }



    private class JSON_PushNewLocationFromMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS3("JSON_PushNewLocationFromMobile");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.i(TAG, "onPostExecute");
            //progressDialog.cancel();
            JSONObject j = null;
            try {

                if (responseJSON.contains("-FAILED-")){
                    //progressDialog.cancel();
                } else if (responseJSON.contains("-SUCCESSFUL-")){
                    //progressDialog.cancel();
                    //db.execSQL("UPDATE reftable SET istreated='YES' WHERE myrefno='"+exprefno+"'");
                }else{
                    //progressDialog.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            //progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
            //progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            //progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS3(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("latitude");
        paramPI.setValue(String.valueOf(latitude));
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("longitude");
        paramPI2.setValue(String.valueOf(longitude));
        paramPI2.setType(String.class);
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("User_FK");
        paramPI3.setValue(String.valueOf(User_Fk));
        paramPI3.setType(String.class);
        request.addProperty(paramPI3);


        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        try {
            // Invole web service
            androidHttpTransport.call(SOAP_ACTION+methName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to static variable
            responseJSON = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            responseJSON="Nothing Returned";
        }
    }


    private String convertToBase64(String imagePath)

    {
        Log.e("Image",imagePath);
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        return encodedImage;
    }
}