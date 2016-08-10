package com.example.pullfrashlistview.view;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.pullfrashlistview.R;
import com.example.pullfrashlistview.interf.OnRefreshListener;

public class RefreshListView extends ListView implements OnScrollListener{
	private static final String TAG = "RefreshListView";
	private final int DOWN_PULL_REFRESH=0; //����ˢ��
	private final int RELEASE_REFRESH=1;//�ɿ�ˢ��
	private final int REFRESHING=2;//����ˢ����
	
    private int firstVisibleItemPosition;
    private int headerViewHeight;
	private int downY; //����ʱY��ƫ����
    private View headerView;
    private int currentState=DOWN_PULL_REFRESH;//ͷ���ֵ�״̬��Ĭ��Ϊ����ˢ��״̬
	
    private Animation downAnimation; //������ת����
	private Animation upAnimation;//������ת����
	
	private ImageView ivArrow;
	private ProgressBar mProgressBar;
	private TextView tvState;
	private TextView tvLastUpdateTime;
	
	private OnRefreshListener mOnRefreshListener;
	private boolean isScrollToButtom;  //�Ƿ񵽴�ײ�
	private View footerView;
	private int footerViewHeight;
	private boolean isLoadingmore=false;//�Ƿ����ڼ��ظ���
	
	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeaderView();
		initFootView();
		this.setOnScrollListener(this);
	}

	/**
	 * ��ʼ���Ų���
	 */
	private void initFootView() {
		footerView = View.inflate(getContext(), R.layout.listview_footer,
				null);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		Log.i("TAG", "������ĸ߶�" + footerViewHeight);
		// headerView.getHeight();//�������ֻ����չʾ����ܻ�ȡheaderview�ĸ߶�
		footerView.setPadding(0, 0, 0, -footerViewHeight);
		
		this.addFooterView(footerView);
	}

	private void initHeaderView() {
		headerView= View.inflate(getContext(), R.layout.listview_header,
				null);
		ivArrow=(ImageView) headerView.findViewById(R.id.iv_listview_header_arrow);
		mProgressBar=(ProgressBar) headerView.findViewById(R.id.pb_listview_header);
		tvState = (TextView) headerView.findViewById(R.id.tv_listview_header_state);
		tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_listview_header_lastupdatetime);
		
		tvLastUpdateTime.setText("���ˢ��ʱ�䣺"+getLastUpdateTime());
		
		// ����ͷ����
		headerView.measure(0, 0);// ϵͳ������ǲ�����headerView�ĸ߶�
		headerViewHeight = headerView.getMeasuredHeight(); // ��ȡ������ĸ߶�
		Log.i("TAG", "������ĸ߶�" + headerViewHeight);
		// headerView.getHeight();//�������ֻ����չʾ����ܻ�ȡheaderview�ĸ߶�
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		
		// LayoutInflater.from(getContext()).inflate(resource, root);
		// �������View������һ���ģ�View��������˷�װ
		// ��listview�Ķ������һ��view
		this.addHeaderView(headerView);
		initAnimation();
	}
	
	/**
	 * ��ȡϵͳ����ʱ��
	 * @return
	 */
	private String getLastUpdateTime(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(System.currentTimeMillis());
	}
	
	/**
	 * ��ʼ������
	 */
	private void initAnimation() {
		upAnimation = new RotateAnimation(
				0f, -180f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f
				);
		upAnimation.setDuration(500);//��������500ms
		upAnimation.setFillAfter(true);//����������ͣ���ڽ�����λ����
		
		downAnimation = new RotateAnimation(
				-180f, -360f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f
				);
		downAnimation.setDuration(500);//��������500ms
		downAnimation.setFillAfter(true);//����������ͣ���ڽ�����λ����
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY=(int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveY=(int) ev.getY();
			
			//�ƶ��е�Y-����ʱ��Y=���
			int diff=(moveY-downY)/2;
			
			//-ͷ���ֵĸ߶�+���=paddingTop
			int paddingTop=-headerViewHeight+diff;
			
			//�����-ͷ���ֵĸ߶�>paddingTop��ֵ ��ִ��super.onTouchEvent(ev);			
			if(firstVisibleItemPosition==0&&-headerViewHeight<paddingTop){
				//Log.i(TAG,"��ǰ�ڶ�������");
				
				if(paddingTop>0&&currentState==DOWN_PULL_REFRESH){  //��ȫ��ʾ��,���ҵ�ǰ״̬������ˢ��״̬
					currentState=RELEASE_REFRESH;
					Log.i(TAG,"�ɿ�ˢ��");
					refreshHeaderView();
				} else if(paddingTop<0&&currentState==RELEASE_REFRESH){//û����ʾ��ȫ
					currentState=DOWN_PULL_REFRESH;
					Log.i(TAG,"����ˢ��");
					refreshHeaderView();
				}
				headerView.setPadding(0, paddingTop, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			//�жϵ�ǰ״̬���ɿ�ˢ�»�������ˢ��
			if(currentState==RELEASE_REFRESH){ //��������ˢ����״̬
				//��ͷ������ʾΪ��ȫ��ʾ
				headerView.setPadding(0,0, 0, 0);				
				currentState=REFRESHING;
				refreshHeaderView();
				
			}else if(currentState==DOWN_PULL_REFRESH){
				//����ͷ����
				headerView.setPadding(0, -headerViewHeight, 0, 0);
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	private void refreshHeaderView(){
		switch (currentState) {
		case DOWN_PULL_REFRESH:
			tvState.setText("����ˢ��");
			ivArrow.startAnimation(downAnimation);
			break;
		case REFRESHING:
			ivArrow.clearAnimation();//�������
			ivArrow.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			tvState.setText("����ˢ����...");
			if(mOnRefreshListener!=null){  //����ʹ���ߵļ����ķ���
				mOnRefreshListener.onDownPullRefresh();
			}
			break;
		case RELEASE_REFRESH:
			tvState.setText("�ɿ�ˢ��");
			ivArrow.startAnimation(upAnimation);
			break;

		default:
			break;
		}
	}
	/**
	 * ������ʱ����
	 * firstVisibleItem����ǰ��ʾ����Ļ������item��position
	 * visibleItemCount:��ǰ��Ļ��ʾ�˶��ٸ���Ŀ������
	 * totalItemCount��ListView������Ŀ������
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		firstVisibleItemPosition=firstVisibleItem;
		if(getLastVisiblePosition()==totalItemCount-1){
             isScrollToButtom = true;
		}else{
			 isScrollToButtom = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState==SCROLL_STATE_IDLE			//����ֹͣ
				||scrollState==SCROLL_STATE_FLING){		//��Ȼ����
			if(isScrollToButtom&&!isLoadingmore){
				isLoadingmore = true;
				//���ظ���
				Log.i(TAG,"���ظ���");
				footerView.setPadding(0, 0, 0, 0);
				this.setSelection(this.getCount());
				if(mOnRefreshListener!=null){
					mOnRefreshListener.onLoadingMore();
				}
			}
		}
	}
	
	public void setOnRefreshListener(OnRefreshListener listener){
		mOnRefreshListener=listener;
	}
	
	
	
	public void hideHeaderView(){
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		ivArrow.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		tvState.setText("����ˢ��");
		tvLastUpdateTime.setText("���ˢ��ʱ�䣺"+getLastUpdateTime());
		currentState=DOWN_PULL_REFRESH;
		
	}
	
	public void hideFooterView(){
		footerView.setPadding(0, 0, 0, -headerViewHeight);
		isLoadingmore = false;
	}
}
