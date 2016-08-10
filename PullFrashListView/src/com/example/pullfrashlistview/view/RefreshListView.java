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
	private final int DOWN_PULL_REFRESH=0; //下拉刷新
	private final int RELEASE_REFRESH=1;//松开刷新
	private final int REFRESHING=2;//正在刷新中
	
    private int firstVisibleItemPosition;
    private int headerViewHeight;
	private int downY; //按下时Y的偏移量
    private View headerView;
    private int currentState=DOWN_PULL_REFRESH;//头布局的状态：默认为下拉刷新状态
	
    private Animation downAnimation; //向下旋转动画
	private Animation upAnimation;//向上旋转动画
	
	private ImageView ivArrow;
	private ProgressBar mProgressBar;
	private TextView tvState;
	private TextView tvLastUpdateTime;
	
	private OnRefreshListener mOnRefreshListener;
	private boolean isScrollToButtom;  //是否到达底部
	private View footerView;
	private int footerViewHeight;
	private boolean isLoadingmore=false;//是否正在加载更多
	
	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeaderView();
		initFootView();
		this.setOnScrollListener(this);
	}

	/**
	 * 初始化脚布局
	 */
	private void initFootView() {
		footerView = View.inflate(getContext(), R.layout.listview_footer,
				null);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		Log.i("TAG", "测量后的高度" + footerViewHeight);
		// headerView.getHeight();//这个方法只有在展示后才能获取headerview的高度
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
		
		tvLastUpdateTime.setText("最后刷新时间："+getLastUpdateTime());
		
		// 隐藏头布局
		headerView.measure(0, 0);// 系统会帮我们测量出headerView的高度
		headerViewHeight = headerView.getMeasuredHeight(); // 获取测量后的高度
		Log.i("TAG", "测量后的高度" + headerViewHeight);
		// headerView.getHeight();//这个方法只有在展示后才能获取headerview的高度
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		
		// LayoutInflater.from(getContext()).inflate(resource, root);
		// 跟上面的View创建是一样的，View对其进行了封装
		// 向listview的顶部添加一个view
		this.addHeaderView(headerView);
		initAnimation();
	}
	
	/**
	 * 获取系统最新时间
	 * @return
	 */
	private String getLastUpdateTime(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(System.currentTimeMillis());
	}
	
	/**
	 * 初始化动画
	 */
	private void initAnimation() {
		upAnimation = new RotateAnimation(
				0f, -180f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f
				);
		upAnimation.setDuration(500);//动画进行500ms
		upAnimation.setFillAfter(true);//动画结束后停留在结束的位置上
		
		downAnimation = new RotateAnimation(
				-180f, -360f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f
				);
		downAnimation.setDuration(500);//动画进行500ms
		downAnimation.setFillAfter(true);//动画结束后停留在结束的位置上
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY=(int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveY=(int) ev.getY();
			
			//移动中的Y-按下时的Y=间距
			int diff=(moveY-downY)/2;
			
			//-头布局的高度+间距=paddingTop
			int paddingTop=-headerViewHeight+diff;
			
			//如果：-头布局的高度>paddingTop的值 ，执行super.onTouchEvent(ev);			
			if(firstVisibleItemPosition==0&&-headerViewHeight<paddingTop){
				//Log.i(TAG,"当前在顶部滑动");
				
				if(paddingTop>0&&currentState==DOWN_PULL_REFRESH){  //完全显示了,并且当前状态是下拉刷新状态
					currentState=RELEASE_REFRESH;
					Log.i(TAG,"松开刷新");
					refreshHeaderView();
				} else if(paddingTop<0&&currentState==RELEASE_REFRESH){//没有显示完全
					currentState=DOWN_PULL_REFRESH;
					Log.i(TAG,"下拉刷新");
					refreshHeaderView();
				}
				headerView.setPadding(0, paddingTop, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			//判断当前状态是松开刷新还是下拉刷新
			if(currentState==RELEASE_REFRESH){ //进入正在刷新中状态
				//把头布局显示为完全显示
				headerView.setPadding(0,0, 0, 0);				
				currentState=REFRESHING;
				refreshHeaderView();
				
			}else if(currentState==DOWN_PULL_REFRESH){
				//隐藏头布局
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
			tvState.setText("下拉刷新");
			ivArrow.startAnimation(downAnimation);
			break;
		case REFRESHING:
			ivArrow.clearAnimation();//清除动画
			ivArrow.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			tvState.setText("正在刷新中...");
			if(mOnRefreshListener!=null){  //调用使用者的监听的方法
				mOnRefreshListener.onDownPullRefresh();
			}
			break;
		case RELEASE_REFRESH:
			tvState.setText("松开刷新");
			ivArrow.startAnimation(upAnimation);
			break;

		default:
			break;
		}
	}
	/**
	 * 当滚动时调用
	 * firstVisibleItem：当前显示在屏幕顶部的item的position
	 * visibleItemCount:当前屏幕显示了多少个条目的总数
	 * totalItemCount：ListView的总条目的条数
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
		if(scrollState==SCROLL_STATE_IDLE			//滑动停止
				||scrollState==SCROLL_STATE_FLING){		//猛然滑动
			if(isScrollToButtom&&!isLoadingmore){
				isLoadingmore = true;
				//加载更多
				Log.i(TAG,"加载更多");
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
		tvState.setText("下拉刷新");
		tvLastUpdateTime.setText("最后刷新时间："+getLastUpdateTime());
		currentState=DOWN_PULL_REFRESH;
		
	}
	
	public void hideFooterView(){
		footerView.setPadding(0, 0, 0, -headerViewHeight);
		isLoadingmore = false;
	}
}
