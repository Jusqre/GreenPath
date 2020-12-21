package com.example.tmapgreentest;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

// 1m 당 lat = 0.000009, long = 0.0000113

@SuppressWarnings("unchecked")
public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback
{

    //버튼들 3개
    Button buttonReco;
    Button buttonSh;
    Button buttonCalcDistance;
    Random rand = new Random();
    int rr = rand.nextInt(30);
    double tradis;
    double retra;
    ArrayList<TMapPoint> arPoint = new ArrayList<>();
    TMapPolyLine tpol = new TMapPolyLine();
    TMapPolyLine typ = new TMapPolyLine();
    //

    Double totaldistance = Double.valueOf(0);
    final TMapData tmapdata = new TMapData();
    //티맵 호출
    private TMapView tMapView = null;
    TMapPoint Userpoint = new TMapPoint(37.276478, 127.041259);
    final TMapPoint tMapPoint1 = new TMapPoint(37.275669, 127.043004);
    final TMapPoint tMapPoint2 = new TMapPoint(37.278669, 127.044004);

    final ArrayList<TMapMarkerItem> atma = new ArrayList();

    int minindex;
    int nextmin;

    double lata = 0;
    double lnga =0;

    ArrayList<TMapPoint> makmak = new ArrayList();
    TMapMarkerItem markmark = new TMapMarkerItem();
    ArrayList makdis = new ArrayList();
    ArrayList<Integer> firquad = new ArrayList();
    ArrayList<Integer> secquad = new ArrayList();
    ArrayList<Integer> thiquad = new ArrayList();
    ArrayList<Integer> fouquad = new ArrayList();

    private boolean tM = true;
    Handler handler = new Handler();

    String currentname;
    final ArrayList<Bitmap> bm = new ArrayList();
    Bitmap bi;

    private Context mContext = this;

    final TMapMarkerItem UserLocation = new TMapMarkerItem();
    TMapGpsManager gps = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tMapView = new TMapView(this);






        //버튼 레이아웃 지정

        buttonReco = findViewById(R.id.buttonReco);
        buttonCalcDistance = findViewById(R.id.buttonCalcDistance);
        buttonSh = findViewById(R.id.buttonsh);

        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.tmap);
        tMapView.setSKTMapApiKey( "YourTMapAPIKEY" );
        linearLayoutTmap.addView( tMapView );

        //티맵 유저 로케이션

        Matrix matrix = new Matrix();
        Bitmap ubi = BitmapFactory.decodeResource(getResources(), R.drawable.user);
        matrix.preScale(0.2f, 0.2f);
        ubi = Bitmap.createBitmap(ubi, 0, 0, ubi.getWidth(), ubi.getHeight(), matrix, false);
        UserLocation.setIcon(ubi);
        Bitmap rbi = BitmapFactory.decodeResource(getResources(), R.drawable.poi_star);

        UserLocation.setIcon(ubi);
        UserLocation.setPosition(0.5f, 1.0f);
        UserLocation.setTMapPoint( Userpoint );
        UserLocation.setName("사용자 현재 위치");
        tMapView.addMarkerItem("User Location", UserLocation);

        TMapGpsManager gps = new TMapGpsManager(this);
        gps.setMinTime(1000);
        gps.setMinDistance(5);
        gps.setProvider(gps.NETWORK_PROVIDER);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }
        gps.OpenGps();
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);

        tMapView.setCenterPoint(gps.getLocation().getLongitude(),gps.getLocation().getLatitude());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tgreentest").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String localName = (String) document.getData().get("name");
                            lata = document.getDouble("relat");
                            lnga = document.getDouble("relng");

                            TMapMarkerItem at= new TMapMarkerItem();
                            at.setIcon(rbi);
                            at.setTMapPoint(new TMapPoint(lata, lnga));
                            at.setName(localName);
                            at.setCanShowCallout(true);
                            at.setCalloutTitle(localName);

                            double k = document.getDouble("area");
                            int redis = (int) Math.round(Math.sqrt(k * 4 * 3.14));
                            int tim = (int) (redis * 60 / 5000);
                            at.setCalloutSubTitle("길이 : " + redis + "m/" + "소요 시간 : " + tim + "분");

                            atma.add(at);
                        }
                    }
                });


        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("산책로 생성");
        alert.setMessage("산책 길이를 입력하십시오.");
        final EditText inputdist = new EditText(this);
        alert.setView(inputdist);
        ExampleThread thread = new ExampleThread();


        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {
                if (thread.isAlive())
                {
                    thread.interrupt();
                }
                ExampleThread thread = new ExampleThread();
                tradis = Integer.parseInt(inputdist.getText().toString());
                if (tradis !=0)
                {
                    thread.start();
                }
            }
        });

        alert.setNeutralButton("reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tMapView.removeAllTMapPolyLine();
                thread.interrupt();
            }
        });

        // 산책로 생성 클릭할 때 온클리커
        buttonCalcDistance.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                if (inputdist.getParent() != null){
                    ((ViewGroup) inputdist.getParent()).removeView(inputdist);}

                AlertDialog MM = alert.create();
                MM.show();
            }
        });


        final AlertDialog.Builder realert = new AlertDialog.Builder(this);

        realert.setTitle("산책로 추천");
        realert.setMessage("산책로 추천 범위를 입력하십시오.");
        final EditText reinput = new EditText(this);
        realert.setView(reinput);
        reThread rethread = new reThread();



        realert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (rethread.isAlive())
                {
                    rethread.interrupt();
                }
                reThread rethread = new reThread();
//

                retra = Integer.parseInt(reinput.getText().toString());
                if (retra != 0)
                {
                    rethread.start();
                }
            }
        });

        realert.setNeutralButton("reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i =0; i< atma.size()-1; i++)
                {
                    atma.get(i).setVisible(TMapMarkerItem.HIDDEN);
                }
            }
        });

        final ArrayList<String> atna = new ArrayList();
        final ArrayList<TMapPoint> atpo = new ArrayList();

        buttonReco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reinput.getParent() != null){
                    ((ViewGroup) reinput.getParent()).removeView(reinput);}

                AlertDialog RR = realert.create();
                RR.show();
            }
        });

        buttonSh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, CommunityActivity.class));
            }
        });
    }
    @Override
    public void onLocationChange(Location location) {
        if (tM){
            tMapView.setLocationPoint(location.getLongitude(),location.getLatitude());
            Userpoint = tMapView.getLocationPoint();
            moveUser();
        }
    }

    public void moveUser()
    {
        tMapView.removeMarkerItem("User Location");

        UserLocation.setPosition(0.5f, 1.0f);
        UserLocation.setTMapPoint( Userpoint );
        UserLocation.setName("사용자 현재 위치");
        tMapView.addMarkerItem("User Location", UserLocation);
    }













    private class reThread extends Thread{
        private static final String TAG = "reThread";

        public reThread() {}

        public void run()
        {
            for (int i=0; i< atma.size()-1; i++)
            {
                TMapPolyLine tol = new TMapPolyLine();
                tol.addLinePoint(Userpoint);
                tol.addLinePoint(atma.get(i).getTMapPoint());
                if (tol.getDistance() <= retra)
                {
                    tMapView.addMarkerItem("q" + i, atma.get(i));
                    atma.get(i).setVisible(TMapMarkerItem.VISIBLE);
                }
            }
        }

    }

    public int finder(TMapPoint a, TMapPoint b)
    {
        if (a.getLatitude()-b.getLatitude()  >0 && a.getLongitude()-b.getLongitude()>0 )
        {
            return 1;
        }
        //2사분면
        if (a.getLatitude()-b.getLatitude()  >0 && a.getLongitude()-b.getLongitude()<0 )
        {
            return 2;
        }
        //3사분면
        if (a.getLatitude()-b.getLatitude()  <0 && a.getLongitude()-b.getLongitude()<0 )
        {
            return 3;
        }
        //4사분면
        if (a.getLatitude()-b.getLatitude()  <0 && a.getLongitude()-b.getLongitude()>0 )
        {
            return 4;
        }
        else
            return 1;
    }

    public void makemak()
    {
        makmak = new ArrayList(); //포지션 저장 어레이리스트
        makdis = new ArrayList();
        markmark = new TMapMarkerItem();

        int i = 0;

        while (i< 100)
        {
            double ran = Math.random() * 2 -1;
            double fan = Math.random() * 2 -1;
            double latq = Userpoint.getLatitude() + 0.000009 * ran * tradis / 2;
            double lngq = Userpoint.getLongitude() + 0.0000113 * fan * tradis / 2;
            TMapPolyLine tol = new TMapPolyLine();
            tol.addLinePoint(Userpoint);
            tol.addLinePoint(new TMapPoint(latq,lngq));
            if (tol.getDistance() < tradis/2)
            {
                makmak.add(new TMapPoint(latq, lngq));
                makdis.add(tol.getDistance());
            }
            i++;
        }

        for(int j=0; j<makmak.size(); j++)
        {
            markmark.setTMapPoint((TMapPoint) makmak.get(j));
            markmark.setPosition(0.5f, 1.0f);
            //tMapView.addMarkerItem("" + j, markmark);
        }
    }

    public void directionsearcher()
    {
        makemak();
        firquad = new ArrayList();
        secquad = new ArrayList();
        thiquad = new ArrayList();
        fouquad = new ArrayList();
        double minval= 1000;

        for(int y =0; y<makmak.size(); y++)
        {
            if ((double) makdis.get(y) < minval)
            {
                minval =(double)  makdis.get(y);
                minindex = y;
            }
        }

        minval= 1000;
        for(int y=0; y<makmak.size(); y++)
        {
            if ((double) makdis.get(y) < minval && y != minindex)
            {
                minval =(double)  makdis.get(y);
                nextmin = y;
            }
        }

        for(int q=0; q<makmak.size()-1; q++)
        {
            //1사분면
            if (finder(makmak.get(q), makmak.get(minindex)) == 1)
            {
                firquad.add(q);
            }
            //2사분면
            if (finder(makmak.get(q), makmak.get(minindex)) == 2 )
            {
                secquad.add(q);
            }
            //3사분면
            if (finder(makmak.get(q), makmak.get(minindex)) == 3 )
            {
                thiquad.add(q);
            }
            //4사분면
            if (finder(makmak.get(q), makmak.get(minindex)) == 4 )
            {
                fouquad.add(q);
            }
        }
    }


    public TMapPoint closest(TMapPoint t, ArrayList<Integer> q)
    {
        TMapPolyLine qt = new TMapPolyLine();
        qt.addLinePoint(t);
        int u = 0;
        double ex = 1000;
        for (int i = 0; i < q.size()-1; i ++)
        {
            TMapPoint e = makmak.get(q.get(i));
            qt.addLinePoint(e);
            double z = qt.getDistance();
            if (ex > z)
            {
                ex = z;
                u = i;
            }
        }
        return makmak.get(q.get(u));
    }


    private class ExampleThread extends Thread{
        private static final String TAG = "ExampleThread";

        public ExampleThread() { }

        public void run()
        {

            TMapCircle tMapCircle = new TMapCircle();
            tMapCircle.setCenterPoint(Userpoint);
            tMapCircle.setRadius(tradis/2);
            tMapCircle.setCircleWidth(2);
            tMapCircle.setLineColor(Color.BLUE);
            tMapCircle.setAreaColor(Color.GRAY);
            tMapCircle.setAreaAlpha(100);
            //tMapView.addTMapCircle("circle1", tMapCircle);

            final int a1 = rr;

            totaldistance = (double) 0;
            Log.d(TAG, firquad + "/" +secquad + "/" +thiquad + "/" + fouquad);

            while(true) {
                if (totaldistance == 0)
                {
                    directionsearcher();
                    arPoint = null;

                    try {
                        typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,Userpoint, (TMapPoint) makmak.get(minindex));
                        typ.setLineColor(Color.BLACK);
                        tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                        totaldistance = typ.getDistance();
                        arPoint = typ.getLinePoint();

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "가장 가까운 마커로 이동" + totaldistance);
                }
                //1사분면
                if (finder(arPoint.get(arPoint.size()-1), arPoint.get(0)) == 1 && totaldistance>0 && totaldistance<tradis *0.3)
                {
                    try {
                        int idx1 = rand.nextInt(firquad.size());
                        typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size()-1), closest(arPoint.get(arPoint.size()-1), firquad));
                        typ.setLineColor(Color.BLUE);
                        tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                        totaldistance = totaldistance + typ.getDistance();
                        arPoint = typ.getLinePoint();
                        Log.d(TAG, "1사분면으로 이동" + totaldistance);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }

                    if (totaldistance>tradis * 0.3 && totaldistance < tradis * 0.6)
                    {
                        try {
                            int idx2 = rand.nextInt(secquad.size());
                            typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size() - 1), closest(arPoint.get(arPoint.size()-1), secquad));
                            typ.setLineColor(Color.RED);
                            tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                            totaldistance = totaldistance + typ.getDistance();
                            arPoint = typ.getLinePoint();
                            Log.d(TAG, "2사분면으로 이동" + totaldistance);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //2사분면
                if (finder(arPoint.get(arPoint.size()-1), arPoint.get(0)) == 2 && totaldistance>0 && totaldistance<tradis *0.3)
                {


                    try {
                        int idx2 = rand.nextInt(secquad.size());
                        typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size()-1), closest(arPoint.get(arPoint.size()-1), secquad));
                        typ.setLineColor(Color.BLUE);
                        tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                        totaldistance = totaldistance + typ.getDistance();
                        arPoint = typ.getLinePoint();
                        Log.d(TAG, "2사분면으로 이동" + totaldistance);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }

                    if (totaldistance>tradis * 0.3 && totaldistance < tradis * 0.6)
                    {
                        try {
                            int idx3 = rand.nextInt(thiquad.size());
                            typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size() - 1), closest(arPoint.get(arPoint.size()-1), thiquad));
                            typ.setLineColor(Color.RED);
                            tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                            totaldistance = totaldistance + typ.getDistance();
                            arPoint = typ.getLinePoint();
                            Log.d(TAG, "3사분면으로 이동" + totaldistance);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }

                }
                //3사분면
                if (finder(arPoint.get(arPoint.size()-1), arPoint.get(0)) == 3 && totaldistance>0 && totaldistance<tradis * 0.3)
                {


                    try {
                        int idx3 =rand.nextInt(thiquad.size());
                        typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size()-1), closest(arPoint.get(arPoint.size()-1), thiquad));
                        typ.setLineColor(Color.BLUE);
                        tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                        totaldistance = totaldistance + typ.getDistance();
                        arPoint = typ.getLinePoint();
                        Log.d(TAG, "3사분면으로 이동" + totaldistance);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }

                    if (totaldistance>tradis * 0.3 && totaldistance < tradis * 0.6)
                    {
                        try {
                            int idx4 = rand.nextInt(fouquad.size());
                            typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size() - 1), closest(arPoint.get(arPoint.size()-1), fouquad));
                            typ.setLineColor(Color.RED);
                            tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                            totaldistance = totaldistance + typ.getDistance();
                            arPoint = typ.getLinePoint();
                            Log.d(TAG, "4사분면으로 이동" + totaldistance);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }

                }
                //4사분면
                if (finder(arPoint.get(arPoint.size()-1), arPoint.get(0)) == 4 && totaldistance>0 && totaldistance<tradis * 0.3)
                {

                    try {
                        int idx4 = rand.nextInt(fouquad.size());
                        typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size()-1), closest(arPoint.get(arPoint.size()-1), fouquad));
                        typ.setLineColor(Color.BLUE);
                        tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                        totaldistance = totaldistance + typ.getDistance();
                        arPoint = typ.getLinePoint();
                        Log.d(TAG, "4사분면으로 이동" +  totaldistance);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }

                    if (totaldistance>tradis *0.3 && totaldistance < tradis * 0.6)
                    {
                        try {
                            int idx1 = rand.nextInt(firquad.size());
                            typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size() - 1), closest(arPoint.get(arPoint.size()-1), firquad));
                            typ.setLineColor(Color.RED);
                            tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                            totaldistance = totaldistance + typ.getDistance();
                            arPoint = typ.getLinePoint();
                            Log.d(TAG, "1사분면으로 이동" + totaldistance);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (totaldistance > tradis * 0.6)
                {
                    try {
                        typ = tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,arPoint.get(arPoint.size() - 1), Userpoint);
                        typ.setLineColor(Color.RED);
                        tMapView.addTMapPolyLine(""+rand.nextInt(30), typ);
                        totaldistance = totaldistance + typ.getDistance();
                        arPoint = typ.getLinePoint();

                        UserLocation.setCanShowCallout(true);
                        UserLocation.setCalloutTitle("총 거리 : "+ (int) Math.round(totaldistance)+"m");
                        int tim = (int) (((int) Math.round(totaldistance))*60 / 5000);
                        UserLocation.setCalloutSubTitle("소요시간 : " + tim + "분" );
                        tMapView.addMarkerItem("ed",UserLocation);

                        Log.d(TAG, "탐색 종료" + totaldistance);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                    if (totaldistance > tradis * 1.30 || totaldistance < tradis * 0.70)
                    {
                        tMapView.removeAllTMapPolyLine();
                        totaldistance = Double.valueOf(0);
                        Log.d(TAG, "생성 실패");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        directionsearcher();
                    }
                    else {
                        Log.d(TAG, "산책로 생성 완료 :" + totaldistance);
                        break;
                    }
                }

                }
            }
        }

}

