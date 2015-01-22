package com.mobilis.android.nfc.server.messages;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.activities.ApplicationActivity;
import com.mobilis.android.nfc.domain.INTENT;
import com.mobilis.android.nfc.model.AbstractModel;
import com.mobilis.android.nfc.model.BanksCodeModel;
import com.mobilis.android.nfc.model.BillPaymentCodesModel;
import com.mobilis.android.nfc.model.EVDCodes;
import com.mobilis.android.nfc.model.LastTransactions;
import com.mobilis.android.nfc.model.LoginBalance;
import com.mobilis.android.nfc.model.TopUpCreditDestinationCodes;
import com.mobilis.android.nfc.model.TopupAirtimeDestinationCodes;
import com.mobilis.android.nfc.model.UtilityCodes;
import com.mobilis.android.nfc.slidemenu.utils.CustomerType;
import com.mobilis.android.nfc.slidemenu.utils.IDType;
import com.mobilis.android.nfc.slidemenu.utils.LoginResponseConstants;
import com.mobilis.android.nfc.slidemenu.utils.Menus;
import com.mobilis.android.nfc.util.BitsMapper;
import com.mobilis.android.nfc.util.Constants;

import java.util.ArrayList;
import java.util.Locale;

import static com.mobilis.android.nfc.util.BitsMapper.getMenuBOptions;
import static com.mobilis.android.nfc.util.BitsMapper.getMenuCOptions;
import static com.mobilis.android.nfc.util.BitsMapper.getMenuContents;
import static com.mobilis.android.nfc.util.BitsMapper.getMenuGOptions;
import static com.mobilis.android.nfc.util.BitsMapper.isShowBalanceAvailable;
import static com.mobilis.android.nfc.util.Constants.setMerchantLoggedin;

/**
 * Created by ahmed on 10/07/14.
 */
public class LoginResponse {

    AbstractModel model;

    public LoginResponse(AbstractModel model){
        this.model = model;
    }

    public void verifyPostResults(){
        model.commitNewTxlId(model.getTxl());
        Log.d("A7mad", "LoginResponse.verifyPostResults() model.getResponseStatus(): "+model.getResponseStatus());
        if(model.getResponseStatus() == model.getActivity().getResources().getInteger(R.integer.NEW_PIN_STATUS_CODE))
        {
            Log.d("A7mad","sending NEW_PIN_REQUIRED intent now");
            LocalBroadcastManager.getInstance(model.getActivity()).sendBroadcast(new Intent(INTENT.NEW_PIN_REQUIRED.toString()));
            return;
        }
        else if (model.getResponseStatus() == model.getResInt(R.string.STATUS_OK)) {
            // This to build MenuProfiles && QuickLinks
            BuildLoggingRequirementsTask task = new BuildLoggingRequirementsTask();
            task.execute();
            if(LoginResponseConstants.accountTabAvailable = checkAccountTabExists()) {
//                getLastTransactions();
                getLoginBalance();
            }
            /** This was moved here because AirTel sends ChangePIN request in LastTransactions responses*/
            getLastTransactions();
            if (LoginResponseConstants.walletOptions.isBillPayments()) {
                getBillPaymentCodes();
                getUtilityCodes();
            }
            if (LoginResponseConstants.walletOptions.isMakePayment()) {
                getBankCodes();
            }
            if (LoginResponseConstants.walletOptions.isSendTopup()) {
                getTopupAirTimeDestinationCodes();
                getTopupCreditDestinationCodes();
            }
            if(LoginResponseConstants.walletOptions.isElectronicVouchersAvailable())
            {
                getEVDCodes();
            }
        }
        else if(model.getResponseStatus() == -43)
            LocalBroadcastManager.getInstance(model.getActivity()).sendBroadcast(new Intent(INTENT.SERVER_COMM_TIME_OUT.toString()).putExtra(INTENT.EXTRA_ERROR.toString(), model.getActivity().getString(R.string.NO_INTERNET_CONNECTION)));
        else if(model.getResponseStatus() == 666)
            LocalBroadcastManager.getInstance(model.getActivity()).sendBroadcast(new Intent(INTENT.SERVER_COMM_TIME_OUT.toString()).putExtra(INTENT.EXTRA_ERROR.toString(), "Shared Preference error"));
        else
            LocalBroadcastManager.getInstance(model.getActivity()).sendBroadcast(new Intent(INTENT.SERVER_COMM_TIME_OUT.toString()).putExtra(INTENT.EXTRA_ERROR.toString(), model.getServerError()));
    }

    private boolean checkAccountTabExists(){
        String menuD = getMenuContents(model.getServerResponse(), model.getResString(R.string.MENU_D));
        Log.d("RUA", "model.getServerResponse(): "+model.getServerResponse());
        Log.d("RUA", "menuD in resource string: "+model.getResString(R.string.MENU_D));
        Log.d("RUA", "menuD: "+menuD);
        boolean temp = isShowBalanceAvailable(menuD);
        Log.d("RUA", "isShowBalanceAvailable: "+temp);
        return temp;
    }

    private boolean checkQuickLinkTabExists(){

        String[] ql = model.getServerResponse().split("QuickLinks=");
        if(ql.length <= 1)
            return false;
        String[] string = ql[1].split("Count=");
        if(string.length <= 1)
            return false;

        return true;
    }
    private void getLoginBalance() {
        LoginBalance loginBalance = new LoginBalance(model.getActivity());
        loginBalance.getConnTaskManager().startBackgroundTask();
    }
    private void getUtilityCodes() {
        UtilityCodes utc = new UtilityCodes(model.getActivity());
        utc.getConnTaskManager().startBackgroundTask();
    }
    private void getTopupCreditDestinationCodes() {
        TopUpCreditDestinationCodes tcdc = new TopUpCreditDestinationCodes(model.getActivity());
        tcdc.getConnTaskManager().startBackgroundTask();
    }
    private void getTopupAirTimeDestinationCodes() {
        TopupAirtimeDestinationCodes tadc = new TopupAirtimeDestinationCodes(model.getActivity());
        tadc.getConnTaskManager().startBackgroundTask();
    }
    private void getBankCodes() {
        BanksCodeModel bcm = new BanksCodeModel(model.getActivity());
        bcm.getConnTaskManager().startBackgroundTask();
    }
    private void getBillPaymentCodes() {
        BillPaymentCodesModel bpc = new BillPaymentCodesModel(model.getActivity());
        bpc.getConnTaskManager().startBackgroundTask();
    }
    private void getLastTransactions() {
        LastTransactions lastTransactions = new LastTransactions(model.getActivity());
        lastTransactions.getConnTaskManager().startBackgroundTask();
    }
    private void getEVDCodes() {
        EVDCodes evd = new EVDCodes(model.getActivity());
        evd.getConnTaskManager().startBackgroundTask();
    }

    private class BuildLoggingRequirementsTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            LoginResponseConstants.customerTypes = getCustomerType(model.getServerResponse());
            LoginResponseConstants.idType = getIdType(model.getServerResponse());
            setMerchantLoggedin(true);
            AbstractModel.saveMerchantIdIfNecessary(model);
            ApplicationActivity.loginClientId = AbstractModel.getCustomerIdFromServerResponse(model.getServerResponse());
            setupProfileMenus();
            if(LoginResponseConstants.quickLinkTabAvailable = checkQuickLinkTabExists())
                setupQuickLinks();
            else if (!LoginResponseConstants.accountTabAvailable) {
                LocalBroadcastManager.getInstance(model.getActivity()).sendBroadcast(new Intent(INTENT.GOT_QUICK_LINKS_PROCEED_TO_LOGIN.toString()));
            }
            LoginResponseConstants.creditCardAvailable = getCreditCardEnabled(model.getServerResponse());
            return null;
        }
        private void setupProfileMenus() {
            if(getMenuContents(model.getServerResponse(), model.getResString(R.string.MENU_B)).equalsIgnoreCase("MenuNotFound")) {
                Toast.makeText(model.getActivity(), "This server is not setup for menu profiling", Toast.LENGTH_LONG).show();
                return;
            }
            Constants.menus = new Menus();
            Constants.menus.setMenuB(getMenuContents(model.getServerResponse(), model.getResString(R.string.MENU_B)));
            Constants.menus.setMenuF(getMenuContents(model.getServerResponse(), model.getResString(R.string.MENU_F)));
            Constants.menus.setMenuBList(getMenuBOptions(Constants.menus.getMenuB(), Constants.menus.getMenuF()));
            Constants.menus.setMenuC(getMenuContents(model.getServerResponse(), model.getResString(R.string.MENU_C)));
            Constants.menus.setMenuCList(getMenuCOptions(Constants.menus.getMenuC()));
            Constants.menus.setMenuG(getMenuContents(model.getServerResponse(), model.getResString(R.string.MENU_G)));
            Constants.menus.setMenuGList(getMenuGOptions(Constants.menus.getMenuG()));
        }


        private void setupQuickLinks() {
            if(!Constants.startedQPProcess){
                Constants.startedQPProcess = true;
                LocalBroadcastManager.getInstance(model.getActivity()).sendBroadcast(new Intent(INTENT.QUICK_LINKS_SETUP.toString()).putExtra(INTENT.EXTRA_RESPONSE.toString(), model.getServerResponse()));
            }
        }

        private ArrayList<CustomerType> getCustomerType(String resp){
            String[] temp = resp.split(",");
            boolean found = false;
            for (String string : temp) {
                if(string.toUpperCase(Locale.US).startsWith("CREATEFLAGS=")){
                    String[] items = string.split("=");
                    if (items.length > 1) {
                        Log.d(LoginResponse.class.getSimpleName(), "Server response CreateFlags: " + items[1]);
                        return BitsMapper.resolveCustomerTypeFromBytes(items[1]);
                    } else {
                        return null;
                    }
                }
            }

            for (String string : temp) {
                if(string.startsWith("CustomerType=")){
                    string = string.replace("CustomerType=", "");
                    Log.d(LoginResponse.class.getSimpleName(), "Server response CustomerType: " + string);
                    if(string.isEmpty())
                        return  null;
                    return BitsMapper.resolveCustomerTypeFromBytes(string);
                }
            }
            return new ArrayList<CustomerType>();
        }

        private ArrayList<IDType> getIdType(String resp){
            String[] temp = resp.split(",");
            for (String string : temp) {
                if(string.startsWith("AllowedIdTypes=")){
                    string = string.replace("AllowedIdTypes=", "");
                    Log.d(LoginResponse.class.getSimpleName(), "Server response  AllowedIdTypes: " + string);
                    if(string.isEmpty())
                        return null;
                    return BitsMapper.resolveIdTypesFromBytes(string);
                }
            }
            return null;
        }

        private boolean getCreditCardEnabled(String resp){
            String[] temp = resp.split(",");
            for (String string : temp) {
                if(string.startsWith("CreditCardEnabled=")){
                    string = string.replace("CreditCardEnabled=", "");
                    Log.d(LoginResponse.class.getSimpleName(), "Server response CreditCardEnabled: " + string);
                    if(string.isEmpty())
                        return false;
                    return true;
                }
            }
            return false;
        }
    }
}
