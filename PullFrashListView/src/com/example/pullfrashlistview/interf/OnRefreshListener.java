package com.example.pullfrashlistview.interf;

/**
 * 
 *�Զ�ˢ��ListView��ˢ�¼����¼�
 */
public interface OnRefreshListener {
	/**
	 * ����ˢ�£�������ɺ���Ҫ��ͷ����Ӱ��
	 */
	void onDownPullRefresh();
	
	/**
	 * ���ظ��࣬������ɺ���Ҫ���Ų���Ӱ��
	 */
	void onLoadingMore();
}
