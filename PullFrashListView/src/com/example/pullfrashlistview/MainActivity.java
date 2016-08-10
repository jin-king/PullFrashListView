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
			textList.add("����һ��ListView������ " + i);
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
		// //����ʼִ������ǰִ�У������߳���ִ�У�һ������µ���������
		// @Override
		// protected void onPreExecute() {
		// super.onPreExecute();
		// }
		//
		// //ִ�������߳��У�����ʱ����
		// @Override
		// protected String doInBackground(String... params) {
		// publishProgress(0);
		// publishProgress(10);
		// publishProgress(20);
		// publishProgress(100);
		// return null;
		// }
		//
		// //�����߳���ִ�У�һ����һЩˢ�½���Ĳ���
		// //����doinbackgroud��ѯ����������ˢ��
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
				textList.add(0, "����ˢ�³���������");
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				//���½���
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
				//���ؽŲ���
				refreshListView.hideFooterView();
				super.onPostExecute(result);
			}

			@Override
			protected Void doInBackground(Void... params) {
				SystemClock.sleep(1000);
				textList.add("�����¼��ص�����1");
				textList.add("�����¼��ص�����2");
				textList.add("�����¼��ص�����3");
				textList.add("�����¼��ص�����4");
				return null;
			}}.execute(new Void[]{});
			
			
	}

}
