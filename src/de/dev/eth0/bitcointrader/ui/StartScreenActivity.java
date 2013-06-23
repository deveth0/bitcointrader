package de.dev.eth0.bitcointrader.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;

/**
 *
 * @author deveth0
 */
public class StartScreenActivity extends AbstractBitcoinTraderActivity {

  private static final String TAG = StartScreenActivity.class.getSimpleName();
  private ProgressDialog mDialog;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_activity);
  }

  @Override
  public void onResume() {
    super.onResume();
    mDialog = new ProgressDialog(this);
    mDialog.setMessage(getString(R.string.connecting_info));
    mDialog.show();
    getBitcoinTraderApplication().startExchangeService();
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, ".onReceive (" + intent.getAction() + ")");
        if (intent.getAction().equals(Constants.UPDATE_SUCCEDED)) {
        mDialog.dismiss();
          startActivity(new Intent(StartScreenActivity.this, BitcoinTraderActivity.class));
        }
        else if (intent.getAction().equals(Constants.UPDATE_FAILED)) {
          //@TODO: notify user that there was an error while connecting
        }
      }
    };
    broadcastManager = LocalBroadcastManager.getInstance(getBitcoinTraderApplication());
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SUCCEDED));
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_FAILED));
  }

  @Override
  public void onPause() {
    super.onPause();
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }
}
