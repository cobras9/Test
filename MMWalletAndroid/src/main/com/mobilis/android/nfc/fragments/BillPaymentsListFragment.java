package com.mobilis.android.nfc.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

import java.util.ArrayList;

/**
 * Created by ahmed on 10/06/14.
 */
public class BillPaymentsListFragment extends ApplicationActivity.PlaceholderFragment{

    private final String TAG = BillPaymentsListFragment.class.getSimpleName();
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_billpayment_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.Fragment_BillPaymentList_ListView);
        final ArrayList<String> values = new ArrayList<String>();
        values.add("Cable TV");
        values.add("Utilities");
        ListViewArrayAdapter adapter = new ListViewArrayAdapter(getActivity(), values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(INTENT.NEW_NEW_FRAGMENT.toString());
                intent.putExtra(INTENT.EXTRA_NUM.toString(), 5);
                intent.putExtra(INTENT.EXTRA_SELECTION.toString(), 4);
                if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.BillPayment_Cable_TV)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.BillPayment_Cable_TV));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.BillPayment_Utilities)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.BillPayment_Utilities));
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
