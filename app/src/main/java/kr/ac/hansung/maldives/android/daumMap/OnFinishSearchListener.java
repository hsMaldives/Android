package kr.ac.hansung.maldives.android.daumMap;

import java.util.List;

import kr.ac.hansung.maldives.android.model.DaumStoreItem;

public interface OnFinishSearchListener {
	public void onSuccess(List<DaumStoreItem> itemList);
	public void onFail();
}
