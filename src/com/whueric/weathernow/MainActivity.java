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

		// WebService地址
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

				// 关闭
				outStream.close();
				is.close();
				connect.disconnect();
				String address = new String(b, "GBK");
				if (address.startsWith("北") || address.startsWith("上")
						|| address.startsWith("重")) {
					setCity(address.substring(0, address.indexOf("市")));
				}
				if (address.startsWith("香")) {
					setCity(address.substring(0, address.indexOf("港")));
				}
				if (address.startsWith("澳")) {
					setCity(address.substring(0, address.indexOf("门")));
				}
				if (address.indexOf("省") != -1) {
					setCity(address.substring(address.indexOf("省") + 1,
							address.indexOf("市")));
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

			// 当天天气
			weatherToday = "\n天气：" + date.split(" ")[1];
			weatherToday = weatherToday + "\n气温："
					+ detail.getProperty(5).toString();
			weatherToday = weatherToday + "\n风力："
					+ detail.getProperty(7).toString() + "\n";

			weatherNow = detail.getProperty(8).toString();
			weatherWillBe = detail.getProperty(9).toString();

			weatherText = getIp() + '\n' + getCity() + "\n今天" + weatherToday;			
			
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
	
				// 关闭
				outStream.close();
				is.close();
				connect.disconnect();
				String address = new String(b, "GBK");
				if (address.startsWith("北") || address.startsWith("上")
						|| address.startsWith("重") || address.startsWith("天") ) {
					setCity(address.substring(0, address.indexOf("市")));
				}
				if (address.startsWith("香")) {
					setCity(address.substring(0, address.indexOf("港")));
				}
				if (address.startsWith("澳")) {
					setCity(address.substring(0, address.indexOf("门")));
				}
				if (address.indexOf("省") != -1) {
					setCity(address.substring(address.indexOf("省") + 1,
							address.indexOf("市")));
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
				
				//实时天气
				weatherNow = detail.getProperty(10).toString();
				
				// 当天天气
				String date = detail.getProperty(6).toString();
				weatherToday = "\n今天 " + date.split(" ")[0] + "\n" + date.split(" ")[1];
				weatherToday = weatherToday + "\n气温："
						+ detail.getProperty(5).toString();
				weatherToday = weatherToday + "\n风力："
						+ detail.getProperty(7).toString() + "\n";

				//明日天气
				String date1 = detail.getProperty(13).toString();
				weatherWillBe = "\n明天 " + date1.split(" ")[0] + "\n" + date1.split(" ")[1];

				weatherWillBe = weatherWillBe + "\n气温："
						+  detail.getProperty(12).toString();
				weatherWillBe = weatherWillBe + "\n风力："
						+ detail.getProperty(14).toString() + "\n";
				/*
				调用方法如下：输入参数：theCityName = 城市中文名称(国外城市可用英文)或城市代码(不输入默认为上海市)，如：上海 或 58367，
				如有城市名称重复请使用城市代码查询(可通过 getSupportCity 或 getSupportDataSet 获得)；返回数据： 一个一维数组 String(22)，共有23个元素。
						String(0) 到 String(4)：省份，城市，城市代码，城市图片名称，最后更新时间。
						String(5) 到 String(11)：当天的 气温，概况，风向和风力，天气趋势开始图片名称(以下称：图标一)，天气趋势结束图片名称(以下称：图标二)，现在的天气实况，天气和生活指数。
						String(12) 到 String(16)：第二天的 气温，概况，风向和风力，图标一，图标二。
						String(17) 到 String(21)：第三天的 气温，概况，风向和风力，图标一，图标二。
						String(22) 被查询的城市或地区的介绍 
				*/
				/*
				anyType{string=直辖市; string=上海; string=58367; string=58367.jpg; string=2013-3-12 15:06:52; string=6℃/22℃; string=3月12日 晴转中雨; 
				        string=南风3-4级转北风4-5级; string=0.gif; string=8.gif; string=今日天气实况：气温：19℃；风向/风力：东风 2级；湿度：49%；空气质量：良；紫外线强度：中等; 
				        string=穿衣指数：建议着大衣、呢外套加毛衣、卫衣等服装。体弱者宜着厚外套、厚毛衣。因昼夜温差较大，注意增减衣服。
						过敏指数：天气条件易诱发过敏，易过敏人群应减少外出，外出宜穿长衣长裤并佩戴好眼镜和口罩，外出归来时及时清洁手和口鼻。
						运动指数：天气较好，但因风力稍强，户外可选择对风力要求不高的运动，推荐您进行室内运动。
						洗车指数：不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。
						晾晒指数：天气不错，适宜晾晒。赶紧把久未见阳光的衣物搬出来吸收一下太阳的味道吧！
						旅游指数：天气晴朗，风稍大，但温度适宜，是个好天气哦。很适宜旅游，您可以尽情地享受大自然的无限风光。
						路况指数：天气较好，路面比较干燥，路况较好。
						舒适度指数：温度适宜，风力不大，您在这样的天气条件下，会感到比较清爽和舒适。
						空气污染指数：气象条件有利于空气污染物稀释、扩散和清除，可在室外正常活动。
						紫外线指数：属中等强度紫外线辐射天气，外出时建议涂擦SPF高于15、PA+的防晒护肤品，戴帽子、太阳镜。; string=4℃/17℃; string=3月13日 中雨转多云; 
						string=北风4-5级转3-4级; string=8.gif; string=1.gif; string=7℃/11℃; string=3月14日 多云; string=东北风3-4级转东风3-4级; 
						string=1.gif; string=1.gif; string=上海简称：沪，位置：上海地处长江三角洲前缘，东濒东海，南临杭州湾，西接江苏，浙江两省，北界长江入海，正当我国南北岸线的中部，
						北纬31°14′，东经121°29′。面积：总面积7823.5平方公里。人口：人口1000多万。上海丰富的人文资源、迷人的城市风貌、繁华的商业街市和欢乐的节庆活动形成了独特的都市景观。
						游览上海，不仅能体验到大都市中西合壁、商儒交融、八方来风的氛围，而且能感受到这个城市人流熙攘、车水马龙、灯火璀璨的活力。上海在中国现代史上占有着十分重要的地位，
						她是中国共产党的诞生地。许多震动中外的历史事件在这里发生，留下了众多的革命遗迹，处处为您讲述着一个个使人永不忘怀的可歌可泣的故事，成为包含民俗的人文景观和纪念地。
						在上海，每到秋祭，纷至沓来的人们在这里祭祀先烈、缅怀革命历史,已成为了一种风俗。大上海在中国近代历史中，曾是风起云涌可歌可泣的地方。在这里荟萃多少风云人物，
						散落在上海各处的不同住宅建筑，由于其主人的非同寻常，蕴含了耐人寻味的历史意义。这里曾留下许多革命先烈的足迹。瞻仰孙中山、宋庆龄、鲁迅等故居，会使您产生抚今追昔的深沉遐思，
						这里还有无数个达官贵人的住宅，探访一下李鸿章、蒋介石等人的公馆，可以联想起主人那段显赫的发迹史。; }
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
