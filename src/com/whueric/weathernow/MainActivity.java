package com.whueric.weathernow;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	public static String weatherText;
	public ImageView imageNow;
	
	//public static TextView contentView;
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		WebServiceCaller caller = new WebServiceCaller();
		caller.execute();
		
		//imageNow = (ImageView) findViewById(R.id.imageNow);  
		
		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section3)
				.setTabListener(this));
		
		
		
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		Fragment fragment = new DummySectionFragment();
		Bundle args = new Bundle();
		args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
				tab.getPosition() + 1);
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment).commit();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Create a new TextView and set its text to the fragment's section
			// number argument value.
			int tabIndex = getArguments().getInt(ARG_SECTION_NUMBER);	
			
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);		
			
			if(1 == tabIndex)
				textView.setText(weatherText);
			
			
			return textView;
		}
	}
	
	
	private class WebServiceCaller extends AsyncTask<String, String, String> {
		
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

			weatherText = getIp() + '\n' + getCity() + "\n����" + weatherToday;			
			
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
						|| address.startsWith("��") || address.startsWith("��") ) {
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
				
				//get weather
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

				String city = detail.getProperty(1).toString();
				setCity(city);
				
				//ʵʱ����
				weatherNow = detail.getProperty(10).toString();
				
				// ��������
				String date = detail.getProperty(6).toString();
				weatherToday = "\n���� " + date.split(" ")[0] + "\n" + date.split(" ")[1];
				weatherToday = weatherToday + "\n���£�"
						+ detail.getProperty(5).toString();
				weatherToday = weatherToday + "\n������"
						+ detail.getProperty(7).toString() + "\n";

				//��������
				String date1 = detail.getProperty(13).toString();
				weatherWillBe = "\n���� " + date1.split(" ")[0] + "\n" + date1.split(" ")[1];

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
				
				result = weatherNow + "\n" + weatherToday + "\n" + weatherWillBe;
				
				//setIcon(weatherNow, imageNow);
					
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
			
			weatherText = getCity() + "\n" + "\n" + result;

			super.onPostExecute(result);
		}	
	}

}
