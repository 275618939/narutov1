package com.movie.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.movie.R;
import com.movie.adapter.MissNarutoQueryAdapter;
import com.movie.app.Constant;
import com.movie.app.Constant.Page;
import com.movie.client.bean.Miss;
import com.movie.client.service.BaseService;
import com.movie.client.service.CallBackService;
import com.movie.network.HttpMissQueryService;
import com.movie.view.LoadView;

public class MissLatelyFragment extends Fragment implements OnClickListener,
		CallBackService, OnRefreshListener2<ListView> {
	public static final int REFRESH_COMPLETE = 0X110;
	ListView missListView;
	LinearLayout loadingLayout;
	LinearLayout loadAfterLayout;
	BaseService missQueryService;
	PullToRefreshListView refreshableListView;
	MissNarutoQueryAdapter missQueryAdapter;
	LoadView loadView;
	List<Miss> misses = new ArrayList<Miss>();
	int page;
	boolean isRefreshing;
	ViewPager pager;
	DisplayMetrics dm;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v= LayoutInflater.from(getActivity()).inflate(R.layout.fragment_miss_lately_query, null);
		missQueryService = new HttpMissQueryService(getActivity());
		loadView = new LoadView(v);
		loadView.showLoadFail(this);
		misses.clear();
		initView(v);
		loadMiss();
		return v;
	}

	public void initView(View view) {
		loadingLayout = (LinearLayout)view.findViewById(R.id.loading);
		loadAfterLayout = (LinearLayout)view.findViewById(R.id.loadAfter);
		missQueryAdapter = new MissNarutoQueryAdapter(getActivity(), mHandler, null);
		refreshableListView = (PullToRefreshListView) view.findViewById(R.id.miss_list);
		refreshableListView.setMode(Mode.BOTH);
		refreshableListView.setOnRefreshListener(this);
		refreshableListView.setAdapter(missQueryAdapter);
		
	}


	private void loadMiss() {
		missQueryService.addUrls(Constant.Miss_Query_API_URL);
		missQueryService.addParams("page", page);
		missQueryService.addParams("size", Page.DEFAULT_SIZE);
		missQueryService.execute(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case REFRESH_COMPLETE:
			loadMiss();
			break;

		default:
			break;

		}
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_COMPLETE:
				loadMiss();
				break;
			default:
				break;

			}
		};
	};

	@Override
	public void SuccessCallBack(Map<String, Object> map) {
		loadView.showLoadAfter(this);
		refreshableListView.onRefreshComplete();
		String code = map.get(Constant.ReturnCode.RETURN_STATE).toString();
		if (Constant.ReturnCode.STATE_1.equals(code)) {
			String tag = map.get(Constant.ReturnCode.RETURN_TAG).toString();
			if (tag.endsWith(missQueryService.TAG)) {
				List<HashMap<String, Object>> datas = (ArrayList<HashMap<String, Object>>) map.get(Constant.ReturnCode.RETURN_VALUE);
				Miss miss = null;
				int size = datas.size();
				HashMap<String, Object> missMap = null;
				for (int i = 0; i < size; i++) {
					miss = new Miss();
					missMap = datas.get(i);
					if (missMap.containsKey("trystId"))
						miss.setTrystId(missMap.get("trystId").toString());
					if (missMap.containsKey("memberId"))
						miss.setMemberId(missMap.get("memberId").toString());
					if (missMap.containsKey("filmId"))
						miss.setFilmId(Integer.parseInt(missMap.get("filmId").toString()));
					if (missMap.containsKey("runTime"))
						miss.setRunTime(missMap.get("runTime").toString());
					if (missMap.containsKey("coin"))
						miss.setCoin(Integer.parseInt(missMap.get("coin").toString()));
					if (missMap.containsKey("cinemaId")) {

					}
					if (missMap.containsKey("attend")) {

					}
					if (missMap.containsKey("status")) {
						miss.setStatus(Integer.parseInt(missMap.get("status").toString()));
					}
					miss.setIcon("http://101.200.176.217/test.jpg");
					misses.add(miss);
				}
				if (size >= Page.DEFAULT_SIZE) {
					page++;
				}

				missQueryAdapter.updateData(misses);
				if (size <= 0) {
					tempData();
				}

			}
		} else {
			tempData();
			String message = map.get(Constant.ReturnCode.RETURN_MESSAGE).toString();
			showToask(message);
		}

	}

	private void tempData() {
		misses=Miss.getTempData();
		missQueryAdapter.updateData(misses);
	}

	@Override
	public void ErrorCallBack(Map<String, Object> map) {

		String message = map.get(Constant.ReturnCode.RETURN_MESSAGE).toString();
		showToask(message);
		tempData();
		loadView.showLoadFail(this);

	}

	@Override
	public void OnRequest() {
		loadView.showLoading(this);
	}

	protected void showToask(String hint) {
		Toast toast = Toast.makeText(getActivity(), hint, Toast.LENGTH_SHORT);
		toast.show();
	}

	// 下拉刷新
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		page = 0;
		loadMiss();
		
	}

	// 上拉Pulling Up
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		page = 1;
		loadMiss();
		

	}

}
