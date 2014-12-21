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
		//位置管理器初始化
		locationManagerProxy = LocationManagerProxy.getInstance(this);
		//找出view
		mapView = (MapView)this.findViewById(R.id.mapview);
		//创建绑定状态
		mapView.onCreate(savedInstanceState);
		//实例化搜索用的按钮和文本框
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
			//获取基本地图view
			aMap = mapView.getMap();
			//设置定位监听,先创建源位置，获取经纬坐标
			aMap.setLocationSource(locationSource);
			
			//设置定位图标,
			aMap.setMyLocationStyle(myLocationStyle);
			//图标显示
			aMap.getUiSettings().setMyLocationButtonEnabled(true);
			//指南针
			aMap.getUiSettings().setCompassEnabled(true);
			//当前位置可见
			aMap.setMyLocationEnabled(true);
			
			/*************************各种事件********************/
			//设置点击marker事件监听器；
			aMap.setOnMarkerClickListener(markerClickListener);
			//设置点击infowindow时间监听器，设置自定义info样式，
			aMap.setInfoWindowAdapter(infoWindowAdapter);
			
		}
		
		//实例化我的位置覆盖物样式
		myLocationStyle = new MyLocationStyle();
		//绘制图标
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
		//设置图标样式
		myLocationStyle.myLocationIcon(bitmapDescriptor);
		//设置周边范围填充颜色
		myLocationStyle.radiusFillColor(this.getResources().getColor(R.color.xx));
		
		
	}
	
	protected void btn_search() {
		progressDialog = ProgressDialog.show(this, "开始搜索", "努力搜索中...");
		initPOI();
	}

	/*********创建源位置，获取经纬坐标(定位源)******/
	//位置管理器,xy代表经纬度
	LocationManagerProxy locationManagerProxy;
	//位置改变监听器
	OnLocationChangedListener locationChangedListener;
	//位置覆盖物样式
	MyLocationStyle myLocationStyle;
	//我的位置，定位源
	LocationSource locationSource = new LocationSource(){
		//定位激活,激活成功，获取返回的数据
		@Override
		public void activate(OnLocationChangedListener arg0) {
			locationChangedListener = arg0;
			/**
			 * 开始定位请求,
			 * 参数一：定位方式（网络定位），
			 * 	     二：定位间隔时间，-1代表只定位一次，
			 * 	     三：    
			 * 	     四：监听器 
			 */
			//管理器请求位置数据，并用监听器监听请求状态
			locationManagerProxy.requestLocationData(LocationManagerProxy.NETWORK_PROVIDER, -1, 0, aMapLocationListener);
		}
		//定位销毁
		@Override
		public void deactivate() {
			//判断位置信息是否为空
			if (locationManagerProxy != null) {
				//如果不为空,清除信息
				locationManagerProxy.removeUpdates(aMapLocationListener);
				locationManagerProxy.destroy();
				locationManagerProxy = null;
			}
			locationChangedListener = null;
		}
	};
	//创建定位监听器（定位请求时调用[获取数据]）
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
			//此方法已废弃
		}
		
		@Override
		public void onLocationChanged(AMapLocation arg0) {
			//如果位置改变了，返回数据参数
			if (locationChangedListener != null && arg0 != null) {
				if (arg0.getAMapException().getErrorCode() == 0) {
					//获取位置信息,经纬度
					Double geoLat = arg0.getLatitude();
					Double geoLng = arg0.getLongitude();
					//接受数据
					locationChangedListener.onLocationChanged(arg0);
				}
			}
		}
	};
	/*********创建源位置，获取经纬坐标(定位源)END******/
	
	/*****************位置搜索服务************************/
	Button btn_search;
	EditText txt_input;
	//搜索进度框
	ProgressDialog progressDialog;
	//当前页码。。。搜结果多时会分页
	int currentPage = 0;
	//关键字搜索条件
	Query query;
	//搜索对象
	PoiSearch poiSearch;
	//搜索结果集合
	List<PoiItem> poiItems;
	//如果没有结果，返回城市建议
	List<SuggestionCity> suggestionCities;
	// 如果关键字输入错误，那么返回建议关键字
	List<String> suggestionKeywords;
	//搜索结果的覆盖物（图标）
	PoiOverlay poiOverlay;
	//POI初始化对象
	public void initPOI(){
		query = new PoiSearch.Query(txt_input.getText().toString(), "","深圳" );
		//设置总页数
		query.setPageSize(20);
		//设置当前页
		query.setPageNum(currentPage);
		
		poiSearch = new PoiSearch(this, query);
		//设置监听器
		poiSearch.setOnPoiSearchListener(onPoiSearchListener);
		//开启一步搜索
		poiSearch.searchPOIAsyn();
	}
	//监听器，关键字搜索结果毁掉
	OnPoiSearchListener onPoiSearchListener = new OnPoiSearchListener() {
			@Override//第二个参数为0，说明有数据
			public void onPoiSearched(PoiResult arg0, int arg1) {
				if (arg1 == 0) {
					//防止空搜索
					if (arg0 != null && arg0.getQuery() != null) {
						//如果搜索的是同一条信息[由于是异步搜索，重复搜索会开启过多线程，占用资源]
						if (arg0.getQuery().equals(query)) {
							//返回搜索的结果集合
							poiItems = arg0.getPois();
							//
							suggestionCities = arg0.getSearchSuggestionCitys();
							suggestionKeywords = arg0.getSearchSuggestionKeywords();
							
							if (poiItems != null && poiItems.size() > 0) {
								aMap.clear();//清空之前数据
								//显示覆盖物
								poiOverlay = new PoiOverlay(aMap, poiItems);
								poiOverlay.removeFromMap();
								poiOverlay.addToMap();
								poiOverlay.zoomToSpan();
							}else{
								Toast.makeText(Act_MyLBS.this, "搜索无结果", 3000).show();
							}
						}
					}
				}else{
					Toast.makeText(Act_MyLBS.this, "搜索失败···", 3000).show();
				}
				progressDialog.dismiss();
			}
			@Override
			public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
			}
		};
	/****************搜索END*************************/
	
	/**************************标记事件*******************/
		//标记点击事件监听器
		OnMarkerClickListener markerClickListener = new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				
				return false;
			}
		};
		//弹出窗口
		OnInfoWindowClickListener infoWindowClickListener = new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker arg0) {
				
			}
		};
		//自定义infowindow样式
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
		
		
	/**************************标记事件END*******************/
		
		/***********其余生命周期******************/
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
	
	/***********生命周期end******************/
	
}
