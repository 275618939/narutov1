package com.movie.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movie.R;
import com.movie.app.MissState;
import com.movie.app.MissStateBackColor;
import com.movie.client.bean.Miss;
import com.movie.ui.MissSelfDetailActivity;
import com.movie.util.ImageLoaderCache;
import com.movie.util.StringUtil;

public class MissSelfQueryAdapter extends BaseAdapter {

	
	
	
	List<Miss> misses;
	Context context;
	LayoutInflater inflater;
	ImageLoaderCache imageLoaderCache;
	Handler handler;
	int missType;

	public MissSelfQueryAdapter(Context context, List<Miss> misses) {
		this.context = context;
		this.misses = misses;
		inflater = LayoutInflater.from(context);
		imageLoaderCache = new ImageLoaderCache(context);

	}
	public MissSelfQueryAdapter(Context context,Handler handler, List<Miss> misses) {
		this.context = context;
		this.misses = misses;
		this.handler=handler;
		inflater = LayoutInflater.from(context);
		imageLoaderCache = new ImageLoaderCache(context);

	}

	@Override
	public int getCount() {
		return misses == null ? 0 : misses.size();
	}

	@Override
	public Miss getItem(int position) {
		if (misses != null && misses.size() != 0) {
			return misses.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder mHolder;
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.miss_slef_item, null);
			mHolder = new ViewHolder();
			mHolder.missItemView = (RelativeLayout) view.findViewById(R.id.miss_item_view);
			mHolder.missIcon = (ImageView) view.findViewById(R.id.miss_icon);
			//mHolder.missUser = (TextView) view.findViewById(R.id.miss_user);
			mHolder.missDate = (TextView) view.findViewById(R.id.miss_date);
			mHolder.missName = (TextView) view.findViewById(R.id.miss_name);
			//mHolder.missAddress = (TextView) view.findViewById(R.id.miss_address);
			mHolder.missBtn = (TextView) view.findViewById(R.id.miss_btn);
			mHolder.missBtnLayout = (LinearLayout) view.findViewById(R.id.miss_btn_layout);
			mHolder.missStageLayout = (LinearLayout) view.findViewById(R.id.miss_stage_layout);
			view.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) view.getTag();
		}
		// 获取position对应的数据
		Miss miss = getItem(position);
		imageLoaderCache.DisplayImage(miss.getIcon(),mHolder.missIcon);
		//mHolder.missUser.setText(miss.getMemberId());
		mHolder.missDate.setText(StringUtil.getShortStrBySym(miss.getRunTime(),":"));
		mHolder.missName.setText(miss.getCinameName());
		//mHolder.missAddress.setText(miss.getCinameAddress());
		int sourceId = MissStateBackColor.getState(miss.getStatus()).getSourceId();
		mHolder.missItemView.setBackgroundResource(sourceId);
		mHolder.missItemView.setOnClickListener(new UserSelectAction(position));
		if(miss.getStatus().intValue()==MissState.Completed.getState()){
			mHolder.missBtnLayout.setVisibility(View.VISIBLE);
			mHolder.missBtn.setText(context.getResources().getString(R.string.branch_coin));
		}
		return view;
	}

	static class ViewHolder {

		RelativeLayout missItemView;
		LinearLayout missBtnLayout;
		LinearLayout missStageLayout;
		// 约会LOGO
		ImageView missIcon;
		// 约会人
		TextView missUser;
		// 约会时间
		TextView missDate;
		// 影片名称
		TextView missName;
		// 约会地址
		TextView missAddress;
		// 约会操作
		TextView missBtn;



	}

	public void updateData(List<Miss> misses) {
		this.misses = misses;
		this.notifyDataSetChanged();

	}
	

	protected class UserSelectAction implements OnClickListener {

		int position;

		public UserSelectAction(int position) {
			this.position = position;
		}

		@Override
		public void onClick(final View v) {

			switch (v.getId()) {
			case R.id.miss_item_view:
				Miss miss = misses.get(position);
				Intent intent = new Intent(context, MissSelfDetailActivity.class);
				intent.putExtra("miss", miss);
				context.startActivity(intent);
				break;
			default:
				break;
			}
			
		}

	}

	public void setMissType(int missType) {
		this.missType = missType;
	}
	

}
