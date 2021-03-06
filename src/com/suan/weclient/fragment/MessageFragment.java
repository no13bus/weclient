/*
 * Copyright (C) 2012 yueyueniao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.suan.weclient.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.suan.weclient.R;
import com.suan.weclient.adapter.MessageListAdapter;
import com.suan.weclient.util.DataManager;
import com.suan.weclient.util.DataManager.MessageChangeListener;
import com.suan.weclient.util.DataManager.UserGroupListener;
import com.suan.weclient.util.MessageHolder;
import com.suan.weclient.util.WechatManager.OnActionFinishListener;

public class MessageFragment extends Fragment implements
		OnRefreshListener<ListView> {
	View view;
	private DataManager mDataManager;
	private PullToRefreshListView pullToRefreshListView;
	private MessageListAdapter messageListAdapter;

	public MessageFragment(DataManager dataManager) {

		mDataManager = dataManager;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.message_fragment, null);

		initWidgets();
		initListener();
		initData();

		return view;
	}

	private void initWidgets() {
		pullToRefreshListView = (PullToRefreshListView) view
				.findViewById(R.id.reply_list);
		pullToRefreshListView.setClickable(true);

		pullToRefreshListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

			}
		});

	}

	private void initData() {

		if (mDataManager.getCurrentMessageHolder() != null) {

			messageListAdapter = new MessageListAdapter(getActivity(),
					mDataManager);
			pullToRefreshListView.setAdapter(messageListAdapter);
			pullToRefreshListView.setOnRefreshListener(MessageFragment.this);

		}

	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void initListener() {

		mDataManager.addUserGroupListener(new UserGroupListener() {
			
			@Override
			public void onGroupChangeEnd() {
				// TODO Auto-generated method stub
				if(mDataManager.getUserGroup().size()==0){
					messageListAdapter.notifyDataSetChanged();
					
				}
				
			}
			
			@Override
			public void onAddUser() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void deleteUser(int index) {
				// TODO Auto-generated method stub
				
			}
		});
		mDataManager.addMessageChangeListener(new MessageChangeListener() {

			@Override
			public void onChange(MessageHolder nowHolder) {
				// TODO Auto-generated method stub
			
				messageListAdapter.notifyDataSetChanged();
				
				
			}
		});

	}

	public DataManager getMessageChangeListener() {
		return mDataManager;
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		new GetDataTask(refreshView).execute();

	}

	private class GetDataTask extends AsyncTask<Void, Void, Void> {

		PullToRefreshBase<?> mRefreshedView;
		private boolean end = false;

		public GetDataTask(PullToRefreshBase<?> refreshedView) {
			mRefreshedView = refreshedView;
			end = false;
			if(mDataManager.getCurrentMessageHolder()==null){
				end = true;
				return;
			}

			try {
				if (mRefreshedView.getCurrentMode() == Mode.PULL_FROM_END) {

					int size = mDataManager.getCurrentMessageHolder()
							.getMessageList().size();
					int page = size / 20
							+ ((size / 20 == 0) ? size % 20 / 10 : 0) + 1;
					mDataManager.getWechatManager().getNextMessageList(page,
							mDataManager.getCurrentPosition(),
							new OnActionFinishListener() {

								@Override
								public void onFinish(Object object) {
									// TODO Auto-generated method stub
									end = true;

								}
							});

				} else if (mRefreshedView.getCurrentMode() == Mode.PULL_FROM_START) {

					mDataManager.getWechatManager().getNewMessageList(false,
							mDataManager.getCurrentPosition(),
							new OnActionFinishListener() {

								@Override
								public void onFinish(Object object) {
									// TODO Auto-generated method stub

									mDataManager.doMessageGet(mDataManager
											.getCurrentMessageHolder());
									end = true;
									mDataManager
											.getWechatManager()
											.getUserProfile(
													false,
													mDataManager
															.getCurrentPosition(),
													new OnActionFinishListener() {

														@Override
														public void onFinish(
																Object object) {
															// TODO
															// Auto-generated
															// method stub

														}
													});
								}
							});

				}
			} catch (Exception e) {
				
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			// Simulates a background job.
			try {

				while (!end) {
					Thread.sleep(50);
				}

			} catch (Exception exception) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mRefreshedView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

}
