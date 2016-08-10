package com.example.pullfrashlistview;

import java.util.ArrayList;
import java.util.List;

import com.example.pullfrashlistview.interf.OnRefreshListener;
import com.example.pullfrashlistview.view.RefreshListView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivity extends Activity implements OnRefreshListener {
	List<String> textList;
	private RefreshListView refreshListView;
	private MyAdapter myAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		refreshListView = (RefreshListView) findViewById(R.id.refreshlistview);
		textList = new ArrayList<String>();
		for (int i = 0; i < 50; i++) {
			textList.add("这是一条ListView的数据 " + i);
		}
		myAdapter = new MyAdapter();
		refreshListView.setAdapter(myAdapter);
		refreshListView.setOnRefreshListener(this);
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return textList.size();
		}

		@Override
		public Object getItem(int position) {
			return textList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(MainActivity.this);
			tv.setText(textList.get(position));
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(18);
			return tv;
		}

	}

	@Override
	public void onDownPullRefresh() {
		// new AsyncTask<String, Integer, String>(){
		//
		// //当开始执行任务前执行，在主线程中执行，一般情况下弹出进度条
		// @Override
		// protected void onPreExecute() {
		// super.onPreExecute();
		// }
		//
		// //执行在子线程中，做耗时操作
		// @Override
		// protected String doInBackground(String... params) {
		// publishProgress(0);
		// publishProgress(10);
		// publishProgress(20);
		// publishProgress(100);
		// return null;
		// }
		//
		// //在主线程中执行，一般做一些刷新界面的操作
		// //根据doinbackgroud查询出来的数据刷新
		// @Override
		// protected void onPostExecute(String result) {
		// super.onPostExecute(result);
		// }
		//
		// @Override
		// protected void onProgressUpdate(Integer... values) {
		// // TODO Auto-generated method stub
		// super.onProgressUpdate(values);
		// }
		//
		//
		// };

		new AsyncTask<String, Integer, Void>() {

			@Override
			protected Void doInBackground(String... params) {
				// TODO Auto-generated method stub
				SystemClock.sleep(1000);
				textList.add(0, "这是刷新出来的数据");
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				//更新界面
				myAdapter.notifyDataSetChanged();
				refreshListView.hideHeaderView();
				super.onPostExecute(result);
			}

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
			}

			

			
		}.execute(new String[]{});
	}

	@Override
	public void onLoadingMore() {
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected void onPostExecute(Void result) {
				// TODO Auto-generated method stub
				myAdapter.notifyDataSetChanged();
				//隐藏脚布局
				refreshListView.hideFooterView();
				super.onPostExecute(result);
			}

			@Override
			protected Void doInBackground(Void... params) {
				SystemClock.sleep(1000);
				textList.add("这是新加载的数据1");
				textList.add("这是新加载的数据2");
				textList.add("这是新加载的数据3");
				textList.add("这是新加载的数据4");
				return null;
			}}.execute(new Void[]{});
			
			
	}

}
