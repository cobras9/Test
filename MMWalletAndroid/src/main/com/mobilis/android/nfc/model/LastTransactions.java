package com.mobilis.android.nfc.model;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.util.Constants;

public class LastTransactions extends AbstractModel {

	private int blockCounter;
	public static final int REQUEST_ATTEMPTS = 5;
	public static final String TXL_HISTORY_COUNT = "20";
    final String TAG = LastTransactions.class.getSimpleName();
   	public LastTransactions(Activity activity){
		super(activity, activity);
		setBlockCounter(1);
	}
	
	private String getCustomerData(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(getResString(R.string.OPEN_BRACKET) + getResString(R.string.REQ_NFCTAGID) + getResString(R.string.EQUAL));
		buffer.append(getAndroidId(getActivity()));
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MOBMONPIN) + getResString(R.string.EQUAL));
		buffer.append(ApplicationActivity.loginClientPin);
		buffer.append(getResString(R.string.COMMA) + getResString(R.string.REQ_MSISDN) + getResString(R.string.EQUAL));
		buffer.append(ApplicationActivity.loginClientId);
		buffer.append(getResString(R.string.CLOSE_BRACKET));
		return new String(buffer);
	}
	
	@Override
	public String getRequestParameters() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFullParamString(getResString(R.string.REQ_MESSAGE_TYPE), getResString(R.string.REQ_MESSAGE_TYPE_LASTTRANSACTIONS), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TERMINAL_ID), getIMEIFromPhone(context), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_PRN_DSP_FORMAT), "D",	false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TXN_HISTORY_COUNT), TXL_HISTORY_COUNT,	false));
		buffer.append(getFullParamString(getResString(R.string.REQ_REQUES_TBLOCK),	String.valueOf(getBlockCounter()-1), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CLIENT_ID), getMerchantId(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_CUST_DATA), getCustomerData(), false));
		buffer.append(getFullParamString(getResString(R.string.REQ_TRANSACTION_ID),	getTransactionId(), true));
		return new String(buffer);
	}

	public String getTransactionId() {
		setTxl(generateTXLId("BalanceHistory"));
		Log.v(TAG,"TxlID straight after constructing it and before i pass it to the server: " + getTxl().getTxlId());
		return getTxl().getTxlId();
	}
	
	@Override
	public void verifyPostTaskResults() {

        if(getResponseStatus() == getActivity().getResources().getInteger(R.integer.NEW_PIN_STATUS_CODE))
        {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.NEW_PIN_REQUIRED.toString()));
            return;
        }
        else if(!LoginResponseConstants.accountTabAvailable)
        {
            // do nothing - Refer to explanation in LoginResponse where it calls getLastTransactions()
        }
		else if(getResponseStatus() == 0 )
		{
		    BalanceExtractorTask task = new BalanceExtractorTask();
            task.execute();
		}
        else
        {
            if(getServerResponse() == null)
                return;
            String error = getMessageFromServerResponse(getServerResponse());
            LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(INTENT.SERVER_COMM_TIME_OUT.toString()).putExtra(INTENT.EXTRA_ERROR.toString(), error));
        }
	}

	private String[] formatPrntBalanceData(String item) {
		Log.d(TAG, "%%%%% formatPrntBalanceData is called");
		String[] prnData = item.split("              ");
//        String[] prnData = item.split("\\s+");
        Log.d(TAG, "prntData.length: "+prnData.length);

		StringBuffer buffer = new StringBuffer();
		for (int i = 1; i < prnData.length; i++) {
			buffer.append(prnData[i]);
		}
		Log.d(TAG, "------(Formatting PrnBalanceData)-----");
		Log.d(TAG,"First buffer is: "+buffer);

		StringBuffer newBuffer = new StringBuffer();
		char delimiter = '|';
		for (int i = 0; i < buffer.length(); i++) {
			if(buffer.charAt(i) == delimiter)
			{
				newBuffer.append(",");
			}
			else
				newBuffer.append(buffer.charAt(i));
		}

		String cleanLine = newBuffer.toString().replace("\\s+", " ");
		Log.d(TAG,"clean line of pritable balances is: "+cleanLine);
		String[] finalArray = cleanLine.toString().split(",");
		return finalArray;
	}
	
	private String[] formatPrntBalanceDataWithIncrementalBlock(String item) {
		Log.d(TAG, "%%%%% formatPrntBalanceDataWithNoRequestTime is called");
		Log.d(TAG,"First buffer is: "+item);
        StringBuffer newBuffer = new StringBuffer();
		char delimiter = '|';
		for (int i = 0; i < item.length(); i++) {
			if(item.charAt(i) == delimiter)
			{
				newBuffer.append(",");
			}
			else
				newBuffer.append(item.charAt(i));
		}
		String cleanLine = newBuffer.toString().replace("\\s+", " ");
		Log.d(TAG,"clean line of pritable balances is: "+cleanLine);
		String[] finalArray = cleanLine.toString().split(",");
		return finalArray;
	}

    private class BalanceExtractorTask extends AsyncTask<Void, Void, Void>{
        private TransactionHistoryGroup mTxlGroup;
        private TransactionHistoryRoot mRoot;
        public BalanceExtractorTask(){
            mRoot = new TransactionHistoryRoot();
            mTxlGroup = new TransactionHistoryGroup();
        }
        @Override
        protected Void doInBackground(Void... params) {
            String[] items = getServerResponse().split(",");

            for (String item : items) {
                if(item.startsWith("PrnData=")) {
                    Log.d(TAG, "Found PrnData item: "+item);

                    String[] cleanLine = null;
                    String requestTime = null;
                    int startIndex = 0;
                    Log.d(TAG, "getBlockCounter(): "+getBlockCounter());

                    if (getBlockCounter() <= 1) {
                        cleanLine = formatPrntBalanceData(item);
                        requestTime = cleanLine[0];
                        startIndex = 1;
                    } else {
                        cleanLine = formatPrntBalanceDataWithIncrementalBlock(item);
                        startIndex = 0;
                    }
                    Log.d(TAG, "cleanLine.length: "+cleanLine.length);
                    for (String s : cleanLine)
                        Log.d(TAG, "LOOPING LINE: "+s);
                    Log.d(TAG, "startIndex: "+startIndex);

                    if (cleanLine.length != 0) {
                        for (int i = startIndex; i < cleanLine.length; i++) {
                            // Ahmed this one here needs to change from splitting according to spaces to :
                            //String[] rowTxl = cleanLine[i].split("\\s+");
                            Log.d(TAG, "Line is: " + cleanLine[i]);
                            String[] rowTxl = cleanLine[i].split(":");
                            if (rowTxl.length > 1) {
                                TransactionHistory tHistory = new TransactionHistory();
                                // create transactionHistory
                                String halfOfTime = rowTxl[0].substring((rowTxl[0].length() - 2));
                                if (rowTxl[0].startsWith("PrnData=(")) {
                                    String tDate = rowTxl[0];
                                    tDate = tDate.replace("PrnData=(", "");
                                    tDate = tDate.substring(0, tDate.length()-2);
                                    tHistory.setTransactionDate(tDate);
                                } else
                                    tHistory.setTransactionDate(rowTxl[0].substring(0, rowTxl[0].length() - 2));

//                                String removeNull = rowTxl[1].toString().replace("null", "    ");
                                tHistory.setTransactionDetails(halfOfTime + ":" + rowTxl[1]);
                                // no transaction groups yet? create the first TransactionHistoryGroup then add it to TransactionHistoryRoot
                                if (this.mTxlGroup.getTransactions().size() == 0) {
                                    this.mTxlGroup.setTransactionDate(tHistory.getTransactionDate());
                                    this.mTxlGroup.getTransactions().add(tHistory);
                                    this.mRoot.getTxlGroup().add(mTxlGroup);
                                    this.mRoot.setRequestTime(requestTime);
                                } else {
                                    int index = -1;
                                    for (int j = 0; j < this.mRoot.getTxlGroup().size(); j++) {
                                        if (tHistory.getTransactionDate().equalsIgnoreCase(mRoot.getTxlGroup().get(j).getTransactionDate())) {
                                            index = j;
                                        }
                                    }
                                    // new transaction
                                    if (index == -1) {
                                        Log.d(TAG, "++++ creating new mTxlGroup with Date: " + tHistory.getTransactionDate());
                                        Log.d(TAG, "adding Transaction Time: " + tHistory.getTransactionDetail());
                                        Log.d(TAG, "adding Transaction Date: " + tHistory.getTransactionDate());
                                        this.mTxlGroup = new TransactionHistoryGroup();
                                        this.mTxlGroup.setTransactionDate(tHistory.getTransactionDate());
                                        this.mTxlGroup.getTransactions().add(tHistory);
                                        this.mRoot.getTxlGroup().add(mTxlGroup);
                                    } else {
                                        Log.d(TAG, "++++ mTxlGroup with date " + tHistory.getTransactionDate() + " already exists");
                                        Log.d(TAG, "adding Transaction Time: " + tHistory.getTransactionDetail());
                                        Log.d(TAG, "adding Transaction Date: " + tHistory.getTransactionDate());
                                        this.mRoot.getTxlGroup().get(index).getTransactions().add(tHistory);
                                    }
                                    Log.d(TAG, "=============================");
                                    Log.d(TAG, "=============================");
                                }
                            }
                        }
                    } else { // empty
                        Log.d(TAG, "Empty");
                        TransactionHistory tHistory = new TransactionHistory();
                        tHistory.setTransactionDate("Transactions");
                        tHistory.setTransactionDetails("No Results");
                        this.mTxlGroup = new TransactionHistoryGroup();
                        this.mTxlGroup.setTransactionDate(tHistory.getTransactionDate());
                        this.mTxlGroup.getTransactions().add(tHistory);
                        this.mRoot.getTxlGroup().add(mTxlGroup);
                        this.mRoot.setRequestTime("SomeString");
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(getBlockCounter() == 1)
                Constants.transactionsRoot = new TransactionHistoryRoot();
            for(TransactionHistoryGroup group: mRoot.getTxlGroup())
                Constants.transactionsRoot.getTxlGroup().add(group);

            if(getBlockCounter() < Integer.parseInt(getResString(R.string.REQUESTS_NUM)))
            {
                LastTransactions balance = new LastTransactions(getActivity());
                balance.setBlockCounter(getBlockCounter()+1);
                balance.getConnTaskManager().startBackgroundTask();
            }
            else
            {
                Log.d(TAG, "getBlockCounter() is 4 sendinf GOT_BALANCE_TRANSACTIONS intent now");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.GOT_BALANCE_TRANSACTIONS.toString()));
            }
//
//		    Constants.transactionsRoot = mRoot;
//		    TransactionHistoryRoot mRoot = Constants.transactionsRoot;
            Log.d(TAG, "BalanceHistory Number of txlGroups inside mRoot is:: "+mRoot.getTxlGroup().size());

            Log.d(TAG, "Number of txlGroups inside mRoot is: "+ mRoot.getTxlGroup().size());
        }
    }

	public int getBlockCounter() {
		return blockCounter;
	}

	public void setBlockCounter(int blockCounter) {
		this.blockCounter = blockCounter;
	}

}
