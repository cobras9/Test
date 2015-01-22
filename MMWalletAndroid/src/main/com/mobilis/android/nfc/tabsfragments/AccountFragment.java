package com.mobilis.android.nfc.tabsfragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.QuickPay;
import com.mobilis.android.nfc.model.TransactionHistory;
import com.mobilis.android.nfc.model.TransactionHistoryGroup;
import com.mobilis.android.nfc.util.Constants;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ahmed on 6/06/14.
 */
public class AccountFragment extends ApplicationActivity.PlaceholderFragment implements View.OnClickListener {

    View rootView;
    ListView listView;
    TextView balanceTV;
    TextView accountIdTV;
    ImageButton refreshButton;
    BroadcastReceiver broadcastReceiver;
    ListViewArrayAdapter adapter;
    ProgressBar progressBar;

    @Override
    public void onResume() {
        super.onResume();
        if (updateBalanceAndLastTransactions()) return;
        if (ApplicationActivity.isRefreshRequired) {
            adapter.notifyDataSetChanged();
            listView.invalidate();
        }
    }


    private boolean updateBalanceAndLastTransactions() {
        Log.d(AccountFragment.class.getSimpleName(), "updateBalanceAndLastTransactions() is called..");
        balanceTV.setText(getAccountBalance());
        QuickPay model = new QuickPay(getActivity());
        accountIdTV.setText(model.getMerchantId());
        model = null;
        if(Constants.transactionsRoot != null)
        {
            ArrayList<TransactionHistory> transactions = new ArrayList<TransactionHistory>();
            ArrayList<TransactionHistoryGroup> txlsGroup = Constants.transactionsRoot.getTxlGroup();
            Log.d("RUI", "txlsGroup.size(): " + txlsGroup.size());
            for (TransactionHistoryGroup transactionHistoryGroup: txlsGroup){
                String date = transactionHistoryGroup.getTransactionDate();
                for (TransactionHistory transactionHistory: transactionHistoryGroup.getTransactions()){
                    transactionHistory.setTransactionDate(date);
                    transactions.add(transactionHistory);
                }
            }
            Log.d("RUI", "transactions.size(): "+transactions.size());

            adapter = new ListViewArrayAdapter(getActivity(), transactions);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            listView.invalidate();
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_account, container, false);
        accountIdTV = (TextView) rootView.findViewById(R.id.Fragment_Account_TextView_MerchantId);
        listView = (ListView) rootView.findViewById(R.id.Fragment_Account_ListView_Transactions);
        balanceTV = (TextView) rootView.findViewById(R.id.Fragment_Account_TextView_Balance);
        refreshButton = (ImageButton) rootView.findViewById(R.id.Fragment_Account_RefreshButton);
        refreshButton.setOnClickListener(this);
        progressBar = (ProgressBar) rootView.findViewById(R.id.Fragment_Account_Progressbar);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(AccountFragment.class.getSimpleName(), "registering broadcast...");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(AccountFragment.class.getSimpleName(), "Received intent "+intent.getAction());
                if(getActivity() == null || !isAdded())
                {
                    return;
                }
                else{
                    updateBalanceAndLastTransactions();
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.UPDATE_ACCOUNTS_BALANCES.toString()));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.REFRESH_ACCOUNT.toString()));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getActivity() == null || getActivity().getFragmentManager() == null)
                    return;
                final FragmentManager fragmentManager = getActivity().getFragmentManager();
                String action = intent.getAction();
                Log.d(AccountFragment.class.getSimpleName(), "received intent: " + intent.getAction());

                if (action.equalsIgnoreCase(INTENT.GOT_BALANCE_TRANSACTIONS.toString())) {
                    updateBalanceAndLastTransactions();
                    hideProgressBar("");
                    return;
                }
                intent=null;
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(INTENT.GOT_BALANCE_TRANSACTIONS.toString()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(AccountFragment.class.getSimpleName(), "onDetach() is called.. removing broadcast receiver...");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);

    }

    private String getAccountBalance() {
        if(ApplicationActivity.ACCOUNT_BALANCE.getDescription() == null && ApplicationActivity.ACCOUNT_BALANCE.getValue() == null)
            return "";
        String description = ApplicationActivity.ACCOUNT_BALANCE.getDescription().toUpperCase(Locale.US);
        String value = ApplicationActivity.ACCOUNT_BALANCE.getValue();
        return description+" : "+value;

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("GINA","onStart() is called");
    }

    private class ListViewArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private ArrayList<TransactionHistory> transactions;

        public ListViewArrayAdapter(Context context, ArrayList<TransactionHistory> transactions) {
            super(context, R.layout.listview_fragment_account_transactions);
            this.context = context;
            this.transactions = transactions;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.listview_fragment_account_transactions, parent, false);
            TextView date = (TextView) rowView.findViewById(R.id.ListView_Fragment_Account_TransactionDate);
            TextView type = (TextView) rowView.findViewById(R.id.ListView_Fragment_Account_TransactionType);
            TextView otherParty = (TextView) rowView.findViewById(R.id.ListView_Fragment_Account_TransactionOtherParty);
            TextView amount = (TextView) rowView.findViewById(R.id.ListView_Fragment_Account_TransactionAmount);
            // it should spit into 4 fields e.g. 18:04  64210513109     1.00DR M2CR
            String[] trans = transactions.get(position).getTransactionDetail().split("\\s+");

            date.setText(transactions.get(position).getTransactionDate());

            if(trans[1].equalsIgnoreCase("null"))
                trans[1] = "";
            if(trans.length >1)
                otherParty.setText(trans[1]);
            if(trans.length >2)
                amount.setText(trans[2]);
            if(trans.length >3)
                type.setText(trans[3]);

            Log.d("RUI","trans.length :"+trans.length);
            return rowView;
        }

        @Override
        public int getCount() {
            Log.d("RUI", "transactions.size(): "+transactions.size());
            return transactions.size();
        }
    }


    @Override
    public void onClick(final View v) {

        switch (v.getId())
        {
            case R.id.Fragment_Account_RefreshButton:
                Log.d(AccountFragment.class.getName(), "clicked refresh button");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(INTENT.UPDATE_BALANCE.toString()));
                showProgressBar();
        }
    }

    private void showProgressBar() {
        refreshButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(String response) {
        refreshButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
}
