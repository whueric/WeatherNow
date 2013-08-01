package com.whueric.weathernow;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.whueric.weathernow.json.JsonAPI;

/**
 * This demonstrates how you can implement switching between the tabs of a
 * TabHost through fragments, using FragmentTabHost.
 */
public class MainTabActivity extends FragmentActivity {
	public String localCityName;
	public String weather_result;
	public String weather_result_query;
	public String aqi_result;
	
	private MapView mMapView = null;	
	private MapController mMapController = null;
	private FrameLayout mMapViewContainer = null;
	private MKMapViewListener mMapListener = null;
	private LocationClient mLocClient;
	//private NotifyLister mNotifyer;
	
	public TextView local_weather = null;
	public TextView query_weather = null;
	public TextView local_aqi = null;
	public TextView query_aqi = null;
	public ImageView image_now = null;
	
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main_tab);          
        
        TabHost host = (TabHost) findViewById(R.id.tabhost);  
        host.setup();  
        host.addTab(host  
                .newTabSpec("t1")  
                .setIndicator(getResources().getString(R.string.title_section1), getResources().getDrawable(R.drawable.icon))  
                .setContent(R.id.sll01));  
        host.addTab(host.newTabSpec("t2").setIndicator(getResources().getString(R.string.title_section2))  
                .setContent(R.id.sll02));  
        host.addTab(host.newTabSpec("t3").setIndicator(getResources().getString(R.string.title_section3))  
                .setContent(R.id.sll03)); 
        
        host.setBackgroundResource(R.drawable.bg);
        
        local_weather = (TextView) findViewById(R.id.textView_local);
        query_weather = (TextView) findViewById(R.id.textView_result);
        local_aqi  = (TextView) findViewById(R.id.textView_aqi_result);
        query_aqi = (TextView) findViewById(R.id.textView_aqi_query);
        image_now = (ImageView) findViewById(R.id.v1);
        
        callWs(null);  
        
        //tab change listener
        OnTabChangeListener tabChangeListen = new OnTabChangeListener() {
        	public void onTabChanged(String tabId) {
        		if(tabId == "t1")
        		{
        			callWs(null);         			
        			local_weather.setText(weather_result);
        		}
        		else if(tabId == "t2")
        		{
        		}
        		else if(tabId == "t3")
        		{
        			query_weather.setText(weather_result_query); 
        			query_aqi.setText(aqi_result);
        		}
        	}    	
        };
        
        host.setOnTabChangedListener(tabChangeListen);    
        
        host.setCurrentTab(0);
        
        //tab3
        Button btn = (Button) this.findViewById(R.id.searchBtn);
        final EditText et = (EditText) this.findViewById(R.id.cityText);
        
        btn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {				
				String city = et.getText().toString();		
				if(city.isEmpty())
					city = localCityName;
				callWs(localCityName);
			}
        	
        }); 
        
        DemoApplication app = (DemoApplication)this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(this);
            app.mBMapManager.init(DemoApplication.strKey,new DemoApplication.MyGeneralListener());
        }

        /*
        mLocClient = new LocationClient( this );
        mLocClient.registerLocationListener( myListener );
        
        //λ��������ش���
        mNotifyer = new NotifyLister();
        mNotifyer.SetNotifyLocation(42.03249652949337,113.3129895882556,3000,"bd09ll");//4����������Ҫλ�����ѵĵ�����꣬���庬������Ϊ��γ�ȣ����ȣ����뷶Χ������ϵ����(gcj02,gps,bd09,bd09ll)
        mLocClient.registerNotify(mNotifyer);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//��gps
        option.setCoorType("bd09ll");     //������������
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
		*/
        
        mMapView = (MapView)findViewById(R.id.bmapView);
        mMapController = mMapView.getController();
        initMapView();
        mMapController.enableClick(true);
        mMapController.setZoom(12);
        mMapView.displayZoomControls(true);
        //mMapController.setCenter();
        //mMapView.setTraffic(true);
        //mMapView.setSatellite(true);
        mMapView.setDoubleClickZooming(true);
        mMapView.setOnTouchListener(null);
       
        mMapListener = new MKMapViewListener() {			
			@Override
			public void onMapMoveFinish() {
			}
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				String title = "";
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
					Toast.makeText(MainTabActivity.this,title,Toast.LENGTH_SHORT).show();
					mMapController.animateTo(mapPoiInfo.geoPt);
				}
			}
		};
		mMapView.regMapViewListener(DemoApplication.getInstance().mBMapManager, mMapListener);			
	}
	
    private void initMapView() {
        GeoPoint centerpt = mMapView.getMapCenter();
        int maxLevel = mMapView.getMaxZoomLevel();
        int zoomlevel = mMapView.getZoomLevel();
        boolean isTraffic = mMapView.isTraffic();
        boolean isSatillite = mMapView.isSatellite();
        boolean isDoubleClick = mMapView.isDoubleClickZooming();
        mMapView.setLongClickable(true);
        //centerpt.setLatitudeE6();
        //centerpt.setLongitudeE6();
        /*
        var map = new BMapManager("container");  
        map.centerAndZoom(new BMap.Point(116.404, 39.915), 11);  
        // ������ַ������ʵ��  
        var myGeo = new BMap.Geocoder();  
        // ����ַ���������ʾ�ڵ�ͼ�ϣ���������ͼ��Ұ  
        myGeo.getPoint("�����к������ϵ� 10 �� 10 ��", function(point){  
        if (point) {  
         map.centerAndZoom(point, 16);  
         map.addOverlay(new BMap.Marker(point));  
        }  
        }, "������")
        */
        //mMapController.setMapClickEnable(true);
       // mMapView.setSatellite(false);
    }

	private void callWs(String cityName) {

		WEATHER_WS_Caller ws_caller = new WEATHER_WS_Caller();
		ws_caller.setCity(cityName);
		ws_caller.execute();
		
		AQI_WS_Caller aqi_caller = new AQI_WS_Caller();
		aqi_caller.setCity(cityName);
		aqi_caller.execute();
	}
	
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        mMapView.destroy();
        super.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	mMapView.onSaveInstanceState(outState);
    	
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	mMapView.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
	
	/*
	protected void onDestroy() {
	    DemoApplication app = (DemoApplication)this.getApplication();
		if (app.mBMapManager != null) {
			app.mBMapManager.destroy();
			app.mBMapManager = null;
		}
		super.onDestroy();
		System.exit(0);
	}
	*/
	
	private class AQI_WS_Caller extends AsyncTask<String, String, String> {

		private String city = null;
		private String ip;
		private boolean isLocal;
		
		@Override
		protected String doInBackground(String... params) {
			String url = "http://pm25.in/api/querys/pm2_5.json?token=D3zyPKFpcUSrMNyM6gSo&city=" + getCity();

			JsonAPI api = new JsonAPI();
			JSONArray jArr;
			JSONObject jobj;
			
			if(city == null) {
				getCityByIp();
				isLocal = true;
			}
			else
				isLocal = false;

			try {
				jobj = api.getMovie(url);
				
				JSONObject jo = (JSONObject) jobj.get("aqi");				
				aqi_result = jo.getString("$t");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return aqi_result;
		}
		
		public void getCityIP() {
			URL url;
			URLConnection conn = null;
			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			String str = "";
			org.jsoup.nodes.Document doc;
			try {
				url = new URL("http://city.ip138.com/city.asp");
				conn = url.openConnection();
				is = conn.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				String input = "";
				while ((input = br.readLine()) != null) {
					str += input;
				}
				doc = Jsoup.parse(str);
				String ip1 = doc.body().text();
				int start = ip1.indexOf("[");
				int end = ip1.indexOf("]");
				setIp(ip1.substring(start + 1, end));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		private void setIp(String substring) {
			ip = substring;			
		}

		private String getCity() {
			return city;
		}

		public void getCityByIp() {
			try {
				URL url = new URL("http://whois.pconline.com.cn/ip.jsp?ip="
						+ getIp());
				HttpURLConnection connect = (HttpURLConnection) url
						.openConnection();
				InputStream is = connect.getInputStream();
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				byte[] buff = new byte[256];
				int rc = 0;
				while ((rc = is.read(buff, 0, 256)) > 0) {
					outStream.write(buff, 0, rc);

				}
				System.out.println(outStream);
				byte[] b = outStream.toByteArray();

				// �ر�
				outStream.close();
				is.close();
				connect.disconnect();
				String address = new String(b, "GBK");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private String getIp() {
			return ip;
		}

		private void setCity(String substring) {
			city = substring;
			
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			if(isLocal)
				local_aqi.setText(result);
			else
				query_aqi.setText(result); 
			
			super.onPostExecute(result);
		}

	};
	
	private class WEATHER_WS_Caller extends AsyncTask<String, String, String> {
		
		String city;
		String ip;
		private static final String WEATHER_WS_NAMESPACE = "http://WebXml.com.cn/";

		// WebService��ַ
		private String WEATHER_WS_URL = "http://www.webxml.com.cn/webservices/weatherwebservice.asmx";
		private static final String METHOD_NAME = "getWeatherbyCityName";

		private String SOAP_ACTION = "http://WebXml.com.cn/getWeatherbyCityName";
		private String weatherToday;
		private SoapObject detail;
		private String weatherNow;
		private String weatherWillBe;
		private Boolean isLocal = false;
		private String weather_icon;

		
		public void getCityIP() {
			URL url;
			URLConnection conn = null;
			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			String str = "";
			org.jsoup.nodes.Document doc;
			try {
				url = new URL("http://city.ip138.com/city.asp");
				conn = url.openConnection();
				is = conn.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				String input = "";
				while ((input = br.readLine()) != null) {
					str += input;
				}
				doc = Jsoup.parse(str);
				String ip1 = doc.body().text();
				int start = ip1.indexOf("[");
				int end = ip1.indexOf("]");
				setIp(ip1.substring(start + 1, end));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public void getCityByIp() {
			try {
				URL url = new URL("http://whois.pconline.com.cn/ip.jsp?ip="
						+ getIp());
				HttpURLConnection connect = (HttpURLConnection) url
						.openConnection();
				InputStream is = connect.getInputStream();
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				byte[] buff = new byte[256];
				int rc = 0;
				while ((rc = is.read(buff, 0, 256)) > 0) {
					outStream.write(buff, 0, rc);

				}
				System.out.println(outStream);
				byte[] b = outStream.toByteArray();

				// �ر�
				outStream.close();
				is.close();
				connect.disconnect();
				String address = new String(b, "GBK");
				if (address.startsWith("��") || address.startsWith("��")
						|| address.startsWith("��")) {
					setCity(address.substring(0, address.indexOf("��")));
				}
				if (address.startsWith("��")) {
					setCity(address.substring(0, address.indexOf("��")));
				}
				if (address.startsWith("��")) {
					setCity(address.substring(0, address.indexOf("��")));
				}
				if (address.indexOf("ʡ") != -1) {
					setCity(address.substring(address.indexOf("ʡ") + 1,
							address.indexOf("��")));
				}
				
				localCityName = getCity();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		public void getWeather(String cityName) {
			try {
				SoapObject rpc = new SoapObject(WEATHER_WS_NAMESPACE, METHOD_NAME);
				rpc.addProperty("theCityName", cityName);

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);
				envelope.bodyOut = rpc;
				envelope.dotNet = true;
				envelope.setOutputSoapObject(rpc);
				HttpTransportSE ht = new HttpTransportSE(WEATHER_WS_URL);

				ht.debug = true;

				ht.call(SOAP_ACTION, envelope);
				detail = (SoapObject) envelope.getResponse();
				parseWeather(detail);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void parseWeather(SoapObject detail)
				throws UnsupportedEncodingException {
			
			String date = detail.getProperty(6).toString();

			// ��������
			weatherToday = "\n������" + date.split(" ")[1];
			weatherToday = weatherToday + "\n���£�"
					+ detail.getProperty(5).toString();
			weatherToday = weatherToday + "\n������"
					+ detail.getProperty(7).toString() + "\n";

			weatherNow = detail.getProperty(8).toString();
			weatherWillBe = detail.getProperty(9).toString();

			weather_result = getIp() + '\n' + getCity() + "\n����" + weatherToday;			
			
		}

		/**
		 * @return the city
		 */
		public String getCity() {
			return city;
		}

		/**
		 * @param city the city to set
		 */
		public void setCity(String city) {
			this.city = city;
		}

		/**
		 * @return the ip
		 */
		public String getIp() {
			return ip;
		}

		/**
		 * @param ip the ip to set
		 */
		public void setIp(String ip) {
			this.ip = ip;
		}

		
		private void setIcon(String weather, ImageView imageview) {
            if (weather.equalsIgnoreCase("nothing.gif"))
                    imageview.setBackgroundResource(R.drawable.a_nothing);
            if (weather.equalsIgnoreCase("0.gif"))
                    imageview.setBackgroundResource(R.drawable.a_0);
            if (weather.equalsIgnoreCase("1.gif"))
                    imageview.setBackgroundResource(R.drawable.a_1);
            if (weather.equalsIgnoreCase("2.gif"))
                    imageview.setBackgroundResource(R.drawable.a_2);
            if (weather.equalsIgnoreCase("3.gif"))
                    imageview.setBackgroundResource(R.drawable.a_3);
            if (weather.equalsIgnoreCase("4.gif"))
                    imageview.setBackgroundResource(R.drawable.a_4);
            if (weather.equalsIgnoreCase("5.gif"))
                    imageview.setBackgroundResource(R.drawable.a_5);
            if (weather.equalsIgnoreCase("6.gif"))
                    imageview.setBackgroundResource(R.drawable.a_6);
            if (weather.equalsIgnoreCase("7.gif"))
                    imageview.setBackgroundResource(R.drawable.a_7);
            if (weather.equalsIgnoreCase("8.gif"))
                    imageview.setBackgroundResource(R.drawable.a_8);
            if (weather.equalsIgnoreCase("9.gif"))
                    imageview.setBackgroundResource(R.drawable.a_9);
            if (weather.equalsIgnoreCase("10.gif"))
                    imageview.setBackgroundResource(R.drawable.a_10);
            if (weather.equalsIgnoreCase("11.gif"))
                    imageview.setBackgroundResource(R.drawable.a_11);
            if (weather.equalsIgnoreCase("12.gif"))
                    imageview.setBackgroundResource(R.drawable.a_12);
            if (weather.equalsIgnoreCase("13.gif"))
                    imageview.setBackgroundResource(R.drawable.a_13);
            if (weather.equalsIgnoreCase("14.gif"))
                    imageview.setBackgroundResource(R.drawable.a_14);
            if (weather.equalsIgnoreCase("15.gif"))
                    imageview.setBackgroundResource(R.drawable.a_15);
            if (weather.equalsIgnoreCase("16.gif"))
                    imageview.setBackgroundResource(R.drawable.a_16);
            if (weather.equalsIgnoreCase("17.gif"))
                    imageview.setBackgroundResource(R.drawable.a_17);
            if (weather.equalsIgnoreCase("18.gif"))
                    imageview.setBackgroundResource(R.drawable.a_18);
            if (weather.equalsIgnoreCase("19.gif"))
                    imageview.setBackgroundResource(R.drawable.a_19);
            if (weather.equalsIgnoreCase("20.gif"))
                    imageview.setBackgroundResource(R.drawable.a_20);
            if (weather.equalsIgnoreCase("21.gif"))
                    imageview.setBackgroundResource(R.drawable.a_21);
            if (weather.equalsIgnoreCase("22.gif"))
                    imageview.setBackgroundResource(R.drawable.a_22);
            if (weather.equalsIgnoreCase("23.gif"))
                    imageview.setBackgroundResource(R.drawable.a_23);
            if (weather.equalsIgnoreCase("24.gif"))
                    imageview.setBackgroundResource(R.drawable.a_24);
            if (weather.equalsIgnoreCase("25.gif"))
                    imageview.setBackgroundResource(R.drawable.a_25);
            if (weather.equalsIgnoreCase("26.gif"))
                    imageview.setBackgroundResource(R.drawable.a_26);
            if (weather.equalsIgnoreCase("27.gif"))
                    imageview.setBackgroundResource(R.drawable.a_27);
            if (weather.equalsIgnoreCase("28.gif"))
                    imageview.setBackgroundResource(R.drawable.a_28);
            if (weather.equalsIgnoreCase("29.gif"))
                    imageview.setBackgroundResource(R.drawable.a_29);
            if (weather.equalsIgnoreCase("30.gif"))
                    imageview.setBackgroundResource(R.drawable.a_30);
            if (weather.equalsIgnoreCase("31.gif"))
                    imageview.setBackgroundResource(R.drawable.a_31);
    	}
		
		
		@Override
		protected String doInBackground(String... params) {
			String result = null;
			try  {
				
				//get weather
				if(city == null)
				{
					getCityByIp();
					isLocal = true;
				}
				else
					isLocal = false;
				
				SoapObject rpc = new SoapObject(WEATHER_WS_NAMESPACE, METHOD_NAME);
				rpc.addProperty("theCityName", city);

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);
				envelope.bodyOut = rpc;
				envelope.dotNet = true;
				envelope.setOutputSoapObject(rpc);
				HttpTransportSE ht = new HttpTransportSE(WEATHER_WS_URL);

				ht.debug = true;

				ht.call(SOAP_ACTION, envelope);
				detail = (SoapObject) envelope.getResponse();

				//String city = detail.getProperty(1).toString();
				//setCity(city);
				
				//ʵʱ����
				weatherNow = detail.getProperty(10).toString()+ "\n";
				
				// ��������
				String date = detail.getProperty(6).toString();
				weatherToday = "����     " + date.split(" ")[0] + "\n" + date.split(" ")[1];
				weatherToday = weatherToday + "\n���£�"
						+ detail.getProperty(5).toString();
				weatherToday = weatherToday + "\n������"
						+ detail.getProperty(7).toString() + "\n";

				//��������
				String date1 = detail.getProperty(13).toString();
				weatherWillBe = "����     " + date1.split(" ")[0] + "\n" + date1.split(" ")[1];

				weatherWillBe = weatherWillBe + "\n���£�"
						+  detail.getProperty(12).toString();
				weatherWillBe = weatherWillBe + "\n������"
						+ detail.getProperty(14).toString() + "\n";
				/*
				���÷������£����������theCityName = ������������(������п���Ӣ��)����д���(������Ĭ��Ϊ�Ϻ���)���磺�Ϻ� �� 58367��
				���г��������ظ���ʹ�ó��д����ѯ(��ͨ�� getSupportCity �� getSupportDataSet ���)���������ݣ� һ��һά���� String(22)������23��Ԫ�ء�
						String(0) �� String(4)��ʡ�ݣ����У����д��룬����ͼƬ���ƣ�������ʱ�䡣
						String(5) �� String(11)������� ���£��ſ�������ͷ������������ƿ�ʼͼƬ����(���³ƣ�ͼ��һ)���������ƽ���ͼƬ����(���³ƣ�ͼ���)�����ڵ�����ʵ��������������ָ����
						String(12) �� String(16)���ڶ���� ���£��ſ�������ͷ�����ͼ��һ��ͼ�����
						String(17) �� String(21)��������� ���£��ſ�������ͷ�����ͼ��һ��ͼ�����
						String(22) ����ѯ�ĳ��л�����Ľ��� 
				*/
				/*
				anyType{string=ֱϽ��; string=�Ϻ�; string=58367; string=58367.jpg; string=2013-3-12 15:06:52; string=6��/22��; string=3��12�� ��ת����; 
				        string=�Ϸ�3-4��ת����4-5��; string=0.gif; string=8.gif; string=��������ʵ�������£�19�棻����/���������� 2����ʪ�ȣ�49%����������������������ǿ�ȣ��е�; 
				        string=����ָ���������Ŵ��¡������׼�ë�¡����µȷ�װ�����������ź����ס���ë�¡�����ҹ�²�ϴ�ע�������·���
						����ָ���������������շ��������׹�����ȺӦ�������������˴����³��㲢������۾��Ϳ��֣��������ʱ��ʱ����ֺͿڱǡ�
						�˶�ָ���������Ϻã����������ǿ�������ѡ��Է���Ҫ�󲻸ߵ��˶����Ƽ������������˶���
						ϴ��ָ��������ϴ����δ��24Сʱ�����꣬����ڴ��ڼ�ϴ������ˮ��·�ϵ���ˮ���ܻ��ٴ�Ū�����İ�����
						��ɹָ������������������ɹ���Ͻ��Ѿ�δ�������������������һ��̫����ζ���ɣ�
						����ָ�����������ʣ����Դ󣬵��¶����ˣ��Ǹ�������Ŷ�����������Σ������Ծ�������ܴ���Ȼ�����޷�⡣
						·��ָ���������Ϻã�·��Ƚϸ��·���Ϻá�
						���ʶ�ָ�����¶����ˣ����������������������������£���е��Ƚ���ˬ�����ʡ�
						������Ⱦָ�����������������ڿ�����Ⱦ��ϡ�͡���ɢ����������������������
						������ָ�������е�ǿ�������߷������������ʱ����Ϳ��SPF����15��PA+�ķ�ɹ����Ʒ����ñ�ӡ�̫������; string=4��/17��; string=3��13�� ����ת����; 
						string=����4-5��ת3-4��; string=8.gif; string=1.gif; string=7��/11��; string=3��14�� ����; string=������3-4��ת����3-4��; 
						string=1.gif; string=1.gif; string=�Ϻ���ƣ�����λ�ã��Ϻ��ش�����������ǰԵ���������������ٺ����壬���ӽ��գ��㽭��ʡ�����糤���뺣�������ҹ��ϱ����ߵ��в���
						��γ31��14�䣬����121��29�䡣����������7823.5ƽ������˿ڣ��˿�1000�����Ϻ��ḻ��������Դ�����˵ĳ��з�ò����������ҵ���кͻ��ֵĽ����γ��˶��صĶ��о��ۡ�
						�����Ϻ������������鵽���������ϱڡ����彻�ڡ��˷�����ķ�Χ�������ܸ��ܵ��������������������ˮ�������ƻ��貵Ļ������Ϻ����й��ִ�ʷ��ռ����ʮ����Ҫ�ĵ�λ��
						�����й��������ĵ����ء�������������ʷ�¼������﷢�����������ڶ�ĸ����ż�������Ϊ��������һ����ʹ�����������Ŀɸ�����Ĺ��£���Ϊ�������׵����ľ��ۺͼ���ء�
						���Ϻ���ÿ��������������������������������ҡ��廳������ʷ,�ѳ�Ϊ��һ�ַ��ס����Ϻ����й�������ʷ�У����Ƿ�����ӿ�ɸ�����ĵط������������Ͷ��ٷ������
						ɢ�����Ϻ������Ĳ�ͬסլ���������������˵ķ�ͬѰ�����̺�������Ѱζ����ʷ���塣�������������������ҵ��㼣��հ������ɽ�������䡢³Ѹ�ȹʾӣ���ʹ����������׷���������˼��
						���ﻹ����������ٹ��˵�סլ��̽��һ������¡�����ʯ���˵Ĺ��ݣ����������������Ƕ��Ժյķ���ʷ��; }
				*/
				
				result = getCity() + "\n\n" + weatherNow + "\n" + weatherToday + "\n" + weatherWillBe;
				weather_icon = detail.getProperty(8).toString();
				
				if(isLocal)
					weather_result = result;
				else
					weather_result_query = result;				
				
				//setIcon(weather_icon, image_now);
					
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			return result;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {		
			if(isLocal)
				local_weather.setText(result);
			else
				query_weather.setText(result); 
			
			//setIcon(weather_icon, image_now);
			
			super.onPostExecute(result);
		}	
	}
	
	/*
	   public class MyLocationListenner implements BDLocationListener {
	        @Override
	        public void onReceiveLocation(BDLocation location) {
	            if (location == null)
	                return ;
	            
	            locData.latitude = location.getLatitude();
	            locData.longitude = location.getLongitude();
	            locData.direction = 2.0f;
	            locData.accuracy = location.getRadius();
	            locData.direction = location.getDerect();
	            Log.d("loctest",String.format("before: lat: %f lon: %f", location.getLatitude(),location.getLongitude()));
	           // GeoPoint p = CoordinateConver.fromGcjToBaidu(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
	          //  Log.d("loctest",String.format("before: lat: %d lon: %d", p.getLatitudeE6(),p.getLongitudeE6()));
	            myLocationOverlay.setData(locData);
	            mMapView.refresh();
	            mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)), mHandler.obtainMessage(1));
	        }
	        
	        public void onReceivePoi(BDLocation poiLocation) {
	            if (poiLocation == null){
	                return ;
	            }
	        }
	    }
	    
	    public class NotifyLister extends BDNotifyListener{
	        public void onNotify(BDLocation mlocation, float distance) {
	        }
	    }
	    */
}