package com.zzj.mylbs.act;

import java.security.KeyStore.LoadStoreParameter;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.LocationSource.OnLocationChangedListener;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.overlay.PoiOverlay;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.zzj.mylbs.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Act_MyLBS extends Activity{
	
	MapView mapView;
	AMap aMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_lbs_main);
		//λ�ù�������ʼ��
		locationManagerProxy = LocationManagerProxy.getInstance(this);
		//�ҳ�view
		mapView = (MapView)this.findViewById(R.id.mapview);
		//������״̬
		mapView.onCreate(savedInstanceState);
		//ʵ���������õİ�ť���ı���
		btn_search = (Button)this.findViewById(R.id.btn_search);
		btn_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_search();
			}
		});
		txt_input = (EditText)this.findViewById(R.id.txt_input);
		//
		if (aMap == null) {
			//��ȡ������ͼview
			aMap = mapView.getMap();
			//���ö�λ����,�ȴ���Դλ�ã���ȡ��γ����
			aMap.setLocationSource(locationSource);
			
			//���ö�λͼ��,
			aMap.setMyLocationStyle(myLocationStyle);
			//ͼ����ʾ
			aMap.getUiSettings().setMyLocationButtonEnabled(true);
			//ָ����
			aMap.getUiSettings().setCompassEnabled(true);
			//��ǰλ�ÿɼ�
			aMap.setMyLocationEnabled(true);
			
			/*************************�����¼�********************/
			//���õ��marker�¼���������
			aMap.setOnMarkerClickListener(markerClickListener);
			//���õ��infowindowʱ��������������Զ���info��ʽ��
			aMap.setInfoWindowAdapter(infoWindowAdapter);
			
		}
		
		//ʵ�����ҵ�λ�ø�������ʽ
		myLocationStyle = new MyLocationStyle();
		//����ͼ��
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
		//����ͼ����ʽ
		myLocationStyle.myLocationIcon(bitmapDescriptor);
		//�����ܱ߷�Χ�����ɫ
		myLocationStyle.radiusFillColor(this.getResources().getColor(R.color.xx));
		
		
	}
	
	protected void btn_search() {
		progressDialog = ProgressDialog.show(this, "��ʼ����", "Ŭ��������...");
		initPOI();
	}

	/*********����Դλ�ã���ȡ��γ����(��λԴ)******/
	//λ�ù�����,xy����γ��
	LocationManagerProxy locationManagerProxy;
	//λ�øı������
	OnLocationChangedListener locationChangedListener;
	//λ�ø�������ʽ
	MyLocationStyle myLocationStyle;
	//�ҵ�λ�ã���λԴ
	LocationSource locationSource = new LocationSource(){
		//��λ����,����ɹ�����ȡ���ص�����
		@Override
		public void activate(OnLocationChangedListener arg0) {
			locationChangedListener = arg0;
			/**
			 * ��ʼ��λ����,
			 * ����һ����λ��ʽ�����綨λ����
			 * 	     ������λ���ʱ�䣬-1����ֻ��λһ�Σ�
			 * 	     ����    
			 * 	     �ģ������� 
			 */
			//����������λ�����ݣ����ü�������������״̬
			locationManagerProxy.requestLocationData(LocationManagerProxy.NETWORK_PROVIDER, -1, 0, aMapLocationListener);
		}
		//��λ����
		@Override
		public void deactivate() {
			//�ж�λ����Ϣ�Ƿ�Ϊ��
			if (locationManagerProxy != null) {
				//�����Ϊ��,�����Ϣ
				locationManagerProxy.removeUpdates(aMapLocationListener);
				locationManagerProxy.destroy();
				locationManagerProxy = null;
			}
			locationChangedListener = null;
		}
	};
	//������λ����������λ����ʱ����[��ȡ����]��
	AMapLocationListener aMapLocationListener = new AMapLocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			//�˷����ѷ���
		}
		
		@Override
		public void onLocationChanged(AMapLocation arg0) {
			//���λ�øı��ˣ��������ݲ���
			if (locationChangedListener != null && arg0 != null) {
				if (arg0.getAMapException().getErrorCode() == 0) {
					//��ȡλ����Ϣ,��γ��
					Double geoLat = arg0.getLatitude();
					Double geoLng = arg0.getLongitude();
					//��������
					locationChangedListener.onLocationChanged(arg0);
				}
			}
		}
	};
	/*********����Դλ�ã���ȡ��γ����(��λԴ)END******/
	
	/*****************λ����������************************/
	Button btn_search;
	EditText txt_input;
	//�������ȿ�
	ProgressDialog progressDialog;
	//��ǰҳ�롣�����ѽ����ʱ���ҳ
	int currentPage = 0;
	//�ؼ�����������
	Query query;
	//��������
	PoiSearch poiSearch;
	//�����������
	List<PoiItem> poiItems;
	//���û�н�������س��н���
	List<SuggestionCity> suggestionCities;
	// ����ؼ������������ô���ؽ���ؼ���
	List<String> suggestionKeywords;
	//��������ĸ����ͼ�꣩
	PoiOverlay poiOverlay;
	//POI��ʼ������
	public void initPOI(){
		query = new PoiSearch.Query(txt_input.getText().toString(), "","����" );
		//������ҳ��
		query.setPageSize(20);
		//���õ�ǰҳ
		query.setPageNum(currentPage);
		
		poiSearch = new PoiSearch(this, query);
		//���ü�����
		poiSearch.setOnPoiSearchListener(onPoiSearchListener);
		//����һ������
		poiSearch.searchPOIAsyn();
	}
	//���������ؼ�����������ٵ�
	OnPoiSearchListener onPoiSearchListener = new OnPoiSearchListener() {
			@Override//�ڶ�������Ϊ0��˵��������
			public void onPoiSearched(PoiResult arg0, int arg1) {
				if (arg1 == 0) {
					//��ֹ������
					if (arg0 != null && arg0.getQuery() != null) {
						//�����������ͬһ����Ϣ[�������첽�������ظ������Ὺ�������̣߳�ռ����Դ]
						if (arg0.getQuery().equals(query)) {
							//���������Ľ������
							poiItems = arg0.getPois();
							//
							suggestionCities = arg0.getSearchSuggestionCitys();
							suggestionKeywords = arg0.getSearchSuggestionKeywords();
							
							if (poiItems != null && poiItems.size() > 0) {
								aMap.clear();//���֮ǰ����
								//��ʾ������
								poiOverlay = new PoiOverlay(aMap, poiItems);
								poiOverlay.removeFromMap();
								poiOverlay.addToMap();
								poiOverlay.zoomToSpan();
							}else{
								Toast.makeText(Act_MyLBS.this, "�����޽��", 3000).show();
							}
						}
					}
				}else{
					Toast.makeText(Act_MyLBS.this, "����ʧ�ܡ�����", 3000).show();
				}
				progressDialog.dismiss();
			}
			@Override
			public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
			}
		};
	/****************����END*************************/
	
	/**************************����¼�*******************/
		//��ǵ���¼�������
		OnMarkerClickListener markerClickListener = new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				
				return false;
			}
		};
		//��������
		OnInfoWindowClickListener infoWindowClickListener = new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker arg0) {
				
			}
		};
		//�Զ���infowindow��ʽ
		InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter() {
			@Override
			public View getInfoWindow(Marker arg0) {
				View view = getLayoutInflater().inflate(R.layout.map_poikeywordsearch_uri, null);
				TextView title = (TextView)view.findViewById(R.id.title);
				title.setText(arg0.getTitle());
				TextView snippet = (TextView)view.findViewById(R.id.snippet);
				snippet.setText(arg0.getSnippet());
				return view;
			}
			@Override
			public View getInfoContents(Marker arg0) {
				return null;
			}
		};
		
		
	/**************************����¼�END*******************/
		
		/***********������������******************/
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mapView.onDestroy();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mapView.onPause();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mapView.onResume();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}
	
	/***********��������end******************/
	
}
