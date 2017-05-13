package kr.ac.hansung.maldives.android.daumMap;

import java.util.List;

import kr.ac.hansung.maldives.model.DaumStoreItem;

public interface OnFinishSearchListener {
	public void onSuccess(List<DaumStoreItem> itemList);
	public void onFail();
}
