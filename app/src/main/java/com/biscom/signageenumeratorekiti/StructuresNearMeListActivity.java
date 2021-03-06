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
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;

public class StructuresNearMeListActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "MAP-RL";
    private static String responseJSON;
    ListView lstStructures;
    TextView txttowntitle;
    Button BtnCancel, btnGetList;
    String getmyuserFK;
    private GoogleApiClient mGoogleApiClient;
    String getmyuserTOKEN;
    Integer TotalValue;
    double latitude;
    double longitude;
    private Location mLastLocation;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 0; // 10 meters
    Integer MAP_ManifestStatusFK, MAP_BillValueFK, MAP_DistanceValueFK;
    Integer HasStructures = 0;
    String MAP_StructureZone;
    private boolean mRequestingLocationUpdates = false;
    private static String neededLongitude;
    private static String neededLatitude;
    private static String neededImageName;
    private static String neededBusinessName;
    private static String neededName;
    private static String neededLocation;
    private static String neededStructureType;
    private static String neededCurrentBilledYear;
    private static String neededNegotiatedAmount;
    private static String neededAmountPaid;
    Integer sItemPosition;
    String cord;
    private JSONArray arrayresultforStructures;
    private ArrayList<String> monthlyincomearraylist;
    private ArrayList<HashMap<String, String>> list;

    ProgressDialog progressDialog;

    public static final String FIRST_COLUMN = "First";
    public static final String SECOND_COLUMN = "Second";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structures_near_me_list);


        monthlyincomearraylist = new ArrayList<String>();
        lstStructures =(ListView)findViewById(R.id.lstStructures);
        txttowntitle =(TextView)findViewById(R.id.txttowntitle);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        btnGetList = (Button) findViewById(R.id.btnGetList);
        Intent intent = getIntent();
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorosams.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyuserFK = sharedPref.getString("User_Fk","0");
        getmyuserTOKEN = sharedPref.getString("User_Fk","0");
        MAP_ManifestStatusFK = sharedPref.getInt("MAP_ManifestStatusFK",0);
        MAP_BillValueFK = sharedPref.getInt("MAP_BillValueFK",0);
        MAP_DistanceValueFK = sharedPref.getInt("MAP_DistanceValueFK",0);
        String MAP_Selected = sharedPref.getString("MAP_Selected","0");
        txttowntitle.setText(MAP_Selected);
       // txttowntitle.setText(MAP_StructureZone);
//        if (HasStructures==0){
//            JSON_ListOfStructure_ByStructureZone_FK task2 = new JSON_ListOfStructure_ByStructureZone_FK();
//            task2.execute();
//        }
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }


        lstStructures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sItemPosition=arg2;
                getneededvalues(sItemPosition);
            //    progressDialog.hide();
                //final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorosams.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                //SharedPreferences.Editor editor = sharedPref.edit();
                //editor.putString("MAP_Latitude", neededLatitude);
                //editor.putString("MAP_Longitude", neededLongitude);
                //editor.putString("MAP_ImageName", neededImageName);
                //editor.putString("MAP_BusinessName", neededBusinessName);
                //editor.commit();

                //Intent myintent = new Intent(MapRecordListActivity.this, MapViewActivity.class);
                //startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MapRecordListActivity.this).toBundle());
                LayoutInflater inflater= LayoutInflater.from(StructuresNearMeListActivity.this);
                View view=inflater.inflate(R.layout.alertview, null);
                TextView textview=(TextView)view.findViewById(R.id.textmsg);
                textview.setText(Html.fromHtml("<b>Business Name:</b> ").toString()+"\n"+neededName+"\n"+"\n"+"Structure Type: "+"\n"+neededStructureType+"\n"+"\n"+"Location: "+"\n"+neededLocation+"\n"+"\n"+"Current Billing Year: "+"\n"+neededCurrentBilledYear+"\n"+"\n"+"Amount Billed: "+"\n"+neededNegotiatedAmount+"\n"+"\n"+"Amount Paid: "+"\n"+neededAmountPaid);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(StructuresNearMeListActivity.this);
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
                        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorosams.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("MAP_Latitude", neededLatitude);
                        editor.putString("MAP_Longitude", neededLongitude);
                        editor.putString("MAP_ImageName", neededImageName);
                        editor.putString("MAP_BusinessName", neededBusinessName);
                        editor.commit();
                        HasStructures=1;
                        Intent myintent = new Intent(StructuresNearMeListActivity.this, MapViewActivity.class);
                        startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(StructuresNearMeListActivity.this).toBundle());
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();



            }
        });

        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnGetList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                        togglePeriodicLocationUpdates();

        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //What todo if there is no permission
            Toast.makeText(StructuresNearMeListActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            //lblLocation.setText(latitude + ", " + longitude);
         //   Toast.makeText(StructuresNearMeListActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();

        } else {
            // latitude = mLastLocation.getLatitude();
            //  longitude = mLastLocation.getLongitude();
            //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            Toast.makeText(StructuresNearMeListActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
            //longitude=0.00;
            //latitude=0.00;
        }
        JSON_SendGPSLocation task = new JSON_SendGPSLocation();
        task.execute();
                Toast.makeText(getBaseContext(), "Longitude: "+longitude +" Latitude: "+ latitude, Toast.LENGTH_LONG).show();
            }
//                Intent myintent = new Intent(MapRecordListActivity.this, MapRecordSelectActivity.class);
//                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MapRecordListActivity.this).toBundle());
//            }


        });
    }



    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }




    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        try{
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }catch(Exception e){

        }

    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        try{
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
        }catch(Exception e){
            return true;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        try{
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }catch(Exception e){

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
        try{
            mGoogleApiClient.connect();
        }catch(Exception e){

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
            stopLocationUpdates();
        }catch(Exception e){

        }

    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        try{
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;
                // Starting the location updates
                startLocationUpdates();
                Log.d(TAG, "Periodic location updates started!");
            } else {
                // Changing the button text
                //btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

                //mRequestingLocationUpdates = false;

                // Stopping the location updates
                //stopLocationUpdates();

                //Log.d(TAG, "Periodic location updates stopped!");
            }
        }catch(Exception e){

        }

    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        try{
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
        }catch(Exception e){

        }

    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        try{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                Toast.makeText(StructuresNearMeListActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch(Exception e){

        }

    }
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }catch(Exception e){

        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        //displayLocation();
        try {
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch (Exception e){

        }

    }
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        try{
            mLastLocation = location;

            //Toast.makeText(getApplicationContext(), "Location changed!",
            //Toast.LENGTH_SHORT).show();
        }catch(Exception e){

        }

        // Displaying the new location on UI
        //displayLocation();
    }



    private class JSON_SendGPSLocation extends AsyncTask<String, String, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS(getmyuserTOKEN,"JSON_SendGPSLocation");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");

            JSONObject j = null;
            try {
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultforStructures = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                getStructures(arrayresultforStructures);
                //pg.setVisibility(View.GONE);

                progressDialog.cancel();
                if (arrayresultforStructures.length()<=0){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(StructuresNearMeListActivity.this);

                    dialog.setTitle( "Structures Near Me" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("No Record to Display")
//  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            }).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(StructuresNearMeListActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

       protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS(String usertoken, String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("ManifestStatus_FK");
        paramPI.setValue(MAP_ManifestStatusFK);
        paramPI.setType(Integer.class);

        PropertyInfo paramPI4 = new PropertyInfo();
        paramPI4.setName("BillValue_FK");
        paramPI4.setValue(MAP_BillValueFK);
        paramPI4.setType(Integer.class);

        PropertyInfo paramPI5 = new PropertyInfo();
        paramPI5.setName("DistanceValue_FK");
        paramPI5.setValue(MAP_DistanceValueFK);
        paramPI5.setType(Integer.class);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("longitude");
        paramPI2.setValue(String.valueOf(longitude));
        paramPI2.setType(String.class);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("latitude");
        paramPI3.setValue(String.valueOf(latitude));
        paramPI3.setType(String.class);



        //pass the structure zone FK and manifest status Fk to the request as a parameter
        // for the webservice
        request.addProperty(paramPI);
        request.addProperty(paramPI2);
        request.addProperty(paramPI3);
        request.addProperty(paramPI4);
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
    private void getStructures(JSONArray j){
        //Traversing through all the items in the json array
        //monthlyincomearraylist.clear();
        list=new ArrayList<HashMap<String,String>>();
        TotalValue=0;
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                HashMap<String,String> temp=new HashMap<String, String>();
                temp.put(FIRST_COLUMN, json.getString("BusinessName"));
                temp.put(SECOND_COLUMN,String.valueOf(Math.round(Double.valueOf(json.getString("Meters")))+"Mtrs"));
                list.add(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getBaseContext(), String.valueOf(list.size())+ " Structures(s)", Toast.LENGTH_LONG).show();
        ListViewAdapters arrayAdapter=new ListViewAdapters(this, list){
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
        //ListViewAdapters adapter=new ListViewAdapters(this, list);
        lstStructures.setAdapter(arrayAdapter);

        // );
    }

    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =arrayresultforStructures.getJSONObject(position);
            //Fetching name from that object
            neededName = json.getString("BusinessName");
            neededLocation = json.getString("StructureLocation_Town");
            neededStructureType = json.getString("TheStructureType");
            neededCurrentBilledYear = json.getString("CurrentBilledYear");
            neededNegotiatedAmount = "N "+String.format("%,.2f",Double.valueOf(json.getString("NegotiatedAmount")));
            neededAmountPaid = "N "+String.format("%,.2f",Double.valueOf(json.getString("AmountPaid")));
            neededImageName = json.getString("ImageName");
            neededLatitude = json.getString("Latitude");
            neededLongitude = json.getString("Longitude");
            neededBusinessName = json.getString("BusinessName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}

