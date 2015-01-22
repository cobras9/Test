package com.mobilis.android.nfc.interfaces;

import com.mobilis.android.nfc.domain.ServerResponse;

public interface Callable {

	public void responseReceivedFromServer(ServerResponse resp);
}
