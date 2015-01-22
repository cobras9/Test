package com.mobilis.android.nfc.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.TransferCode;

import java.util.ArrayList;

/**
 * Created by ahmed on 10/06/14.
 */
public class SendMoneyListFragment extends ApplicationActivity.PlaceholderFragment{

    private final String TAG = SendMoneyListFragment.class.getSimpleName();
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_send_money_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.Fragment_SendMoneyList_ListView);
        final ArrayList<String> values = new ArrayList<String>();
        values.add(getString(R.string.app_name));

        Log.d("AhmedFAR","Checking bankcode size: "+TransferCode.bankCodes.size());
        if(TransferCode.bankCodes.size() != 0)
            values.add(getString(R.string.BANKS));
        if(TransferCode.topupcreditCodes.size() != 0)
            values.add(getString(R.string.OTHER_OPERATORS));
        ListViewArrayAdapter adapter = new ListViewArrayAdapter(getActivity(), values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(INTENT.NEW_NEW_FRAGMENT.toString());
                intent.putExtra(INTENT.EXTRA_NUM.toString(), 5);
                intent.putExtra(INTENT.EXTRA_SELECTION.toString(), 4);
                if(values.get(position).equalsIgnoreCase(getString(R.string.app_name)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getString(R.string.app_name));
                else if(values.get(position).equalsIgnoreCase(getString(R.string.BANKS)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getString(R.string.BANKS));
                else if(values.get(position).equalsIgnoreCase(getString(R.string.OTHER_OPERATORS)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getString(R.string.OTHER_OPERATORS));
                LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                            new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString()).putExtra(INTENT.EXTRA_TITLE.toString(),values.get(position)));
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
//        hideKeyboard();
    }

    private class ListViewArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private ArrayList<String> values;

        public ListViewArrayAdapter(Context context, ArrayList<String> values) {
            super(context, R.layout.listview_fragment_account_transactions);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.listview_fragment_other_list, parent, false);
            TextView option = (TextView) rowView.findViewById(R.id.ListView_Fragment_Other_TextView_OptionName);
            option.setText(values.get(position));
            return rowView;
        }

        @Override
        public int getCount() {
            return values.size();
        }
    }


}
