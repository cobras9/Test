package com.mobilis.android.nfc.tabsfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.fragments.NFCFragment;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.util.NFCForegroundUtil;

import java.util.ArrayList;

/**
 * Created by ahmed on 6/06/14.
 */
public class MenuFragment extends ApplicationActivity.PlaceholderFragment{

    ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_other, container, false);
        listView = (ListView) rootView.findViewById(R.id.Fragment_Other_ListView);
        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();
        final ArrayList<String> values = new ArrayList<String>();

        if(LoginResponseConstants.walletOptions.isMakePayment())
            values.add(getResources().getString(R.string.MAKE_PAYMENT));

        if(LoginResponseConstants.walletOptions.isReceivePaymentAvailable() && NFCFragment.supportsNFC(new NFCForegroundUtil(getActivity())))
            values.add(getResources().getString(R.string.RECEIVE_PAYMENT));

        if(LoginResponseConstants.walletOptions.isGiveCashOutAvailable() && NFCFragment.supportsNFC(new NFCForegroundUtil(getActivity())))
            values.add(getResources().getString(R.string.GIVE_CASH_OUT));

        if(LoginResponseConstants.walletOptions.isReceiveCashInAvailable()) {
            values.add(getResources().getString(R.string.RECEIVE_CASH_IN));
//            values.add(getResources().getString(R.string.RECEIVE_CASH_IN_CC));
        }

        if(LoginResponseConstants.walletOptions.isRedeemVoucherAvailable())
            values.add(getResources().getString(R.string.REDEEM_VOUCHER));

        if(LoginResponseConstants.walletOptions.isCashoutVouchers())
            values.add(getResources().getString(R.string.CASH_OUT_VOUCHER));

        if(LoginResponseConstants.walletOptions.isElectronicVouchersAvailable()) {
            values.add(getString(R.string.SELL_VOUCHER));
        }

        if (LoginResponseConstants.walletOptions.isTxfEVDAvailable()) {
            values.add(getString(R.string.CASH_IN_VOUCHER));
        }

        if(LoginResponseConstants.walletOptions.isBuyVoucherAvailable())
        {
            values.add(getString(R.string.BUY_VOUCHER));
        }

        if (LoginResponseConstants.walletOptions.isBuyBulkVoucherAvailable()){
            values.add(getString(R.string.BUY_BULK_VOUCHER));
        }

        if(LoginResponseConstants.walletOptions.isBillPayments())
            values.add(getResources().getString(R.string.BillPayments));

        if(LoginResponseConstants.walletOptions.isSendTopup())
            values.add(getResources().getString(R.string.TOP_UP));

        if(LoginResponseConstants.walletOptions.isRegServicesTag())
            values.add(getResources().getString(R.string.TAG_REGISTRATION));

        if(LoginResponseConstants.walletOptions.isChangePinAvailable())
            values.add(getResources().getString(R.string.CHANGE_PIN));

        if(LoginResponseConstants.walletOptions.isCustomerLookupAvailable())
            values.add(getString(R.string.CUSTOMER_REGISTRATION));

        if(LoginResponseConstants.walletOptions.isServerConfigurable())
            values.add(getResources().getString(R.string.CONFIG_SERVER));

        if(LoginResponseConstants.walletOptions.isSelectCurrencyAvailable())
            values.add(getResources().getString(R.string.SELECT_CURRENCY));

        if(LoginResponseConstants.walletOptions.isReceiveCashInCCAvailable()) {
            values.add(getResources().getString(R.string.RECEIVE_CASH_IN_CC));
        }

        if(LoginResponseConstants.walletOptions.isGenerateTokenAvailable()) {
            values.add(getResources().getString(R.string.GENERATE_TOKEN));
        }

        ListViewArrayAdapter adapter = new ListViewArrayAdapter(getActivity(), values);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(INTENT.NEW_FRAGMENT.toString());
                intent.putExtra(INTENT.EXTRA_NUM.toString(), 4);
                intent.putExtra(INTENT.EXTRA_SELECTION.toString(), 3);
                if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.CUSTOMER_REGISTRATION)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.CUSTOMER_REGISTRATION));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.MAKE_PAYMENT)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.MAKE_PAYMENT));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.RECEIVE_PAYMENT))) // This is purchase
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.RECEIVE_PAYMENT));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.GIVE_CASH_OUT)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.GIVE_CASH_OUT));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.RECEIVE_CASH_IN)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.RECEIVE_CASH_IN));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.RECEIVE_CASH_IN_CC)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.RECEIVE_CASH_IN_CC));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.REDEEM_VOUCHER)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.REDEEM_VOUCHER));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.CASH_OUT_VOUCHER)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.CASH_OUT_VOUCHER));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.TOP_UP)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.TOP_UP));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.BillPayments)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.BillPayments));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.CHANGE_PIN)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.CHANGE_PIN));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.TAG_REGISTRATION)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.TAG_REGISTRATION));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.CONFIG_SERVER)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.CONFIG_SERVER));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.SELECT_CURRENCY)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.SELECT_CURRENCY));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.CASH_IN_VOUCHER)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.CASH_IN_VOUCHER));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.SELL_VOUCHER)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.SELL_VOUCHER));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.BUY_VOUCHER)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.BUY_VOUCHER));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.BUY_BULK_VOUCHER)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.BUY_BULK_VOUCHER));
                else if(values.get(position).equalsIgnoreCase(getResources().getString(R.string.GENERATE_TOKEN)))
                    intent.putExtra(INTENT.EXTRA_FRAG_NAME.toString(), getResources().getString(R.string.GENERATE_TOKEN));


                LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(INTENT.UPDATE_ACTION_BAR_TITLE.toString()).putExtra(INTENT.EXTRA_TITLE.toString(),values.get(position)));
            }
        });
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

            ImageView imageView = (ImageView) rowView.findViewById(R.id.ListView_Fragment_Other_ImageView);
            setImageView(values.get(position), imageView);
            return rowView;
        }

        @Override
        public int getCount() {
            return values.size();
        }

        private void setImageView(String text, ImageView imageView){
            if(text.equalsIgnoreCase(getResources().getString(R.string.MAKE_PAYMENT)))
                imageView.setBackgroundResource(R.drawable.send_money);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.RECEIVE_PAYMENT)))
                imageView.setBackgroundResource(R.drawable.receive_money);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.GIVE_CASH_OUT)))
                imageView.setBackgroundResource(R.drawable.cashout);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.RECEIVE_CASH_IN)))
                imageView.setBackgroundResource(R.drawable.recieve_cash_in);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.RECEIVE_CASH_IN_CC)))
                imageView.setBackgroundResource(R.drawable.recieve_cash_in);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.REDEEM_VOUCHER)))
                imageView.setBackgroundResource(R.drawable.redeem_voucher);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.CASH_IN_VOUCHER)))
                imageView.setBackgroundResource(R.drawable.redeem_voucher);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.SELL_VOUCHER)))
                imageView.setBackgroundResource(R.drawable.redeem_voucher);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.BUY_VOUCHER)))
                imageView.setBackgroundResource(R.drawable.redeem_voucher);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.BUY_BULK_VOUCHER)))
                imageView.setBackgroundResource(R.drawable.redeem_voucher);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.CASH_OUT_VOUCHER)))
                imageView.setBackgroundResource(R.drawable.redeem_voucher);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.BillPayments)))
                imageView.setBackgroundResource(R.drawable.bill_payment);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.TOP_UP)))
                imageView.setBackgroundResource(R.drawable.air_time_top);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.TAG_REGISTRATION)))
                imageView.setBackgroundResource(R.drawable.registration);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.CHANGE_PIN)))
                imageView.setBackgroundResource(R.drawable.changepin);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.SELECT_CURRENCY)))
                imageView.setBackgroundResource(R.drawable.currency_selection);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.CONFIG_SERVER)))
                imageView.setBackgroundResource(R.drawable.server_configuration);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.CUSTOMER_REGISTRATION)))
                imageView.setBackgroundResource(R.drawable.menu_registration);
            else if(text.equalsIgnoreCase(getResources().getString(R.string.GENERATE_TOKEN)))
                imageView.setBackgroundResource(R.drawable.redeem_voucher);

        }
    }
}
