package com.example.pullfrashlistview.interf;

/**
 * 
 *自动刷新ListView的刷新监听事件
 */
public interface OnRefreshListener {
	/**
	 * 下拉刷新，下拉完成后需要将头布局影藏
	 */
	void onDownPullRefresh();
	
	/**
	 * 加载更多，加载完成后需要将脚布局影藏
	 */
	void onLoadingMore();
}
