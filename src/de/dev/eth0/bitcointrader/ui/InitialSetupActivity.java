//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import si.mazi.rescu.HttpException;

//@TOOD: Testing Data Dialog disappears if device rotated
public class InitialSetupActivity extends AbstractBitcoinTraderActivity {

  private static final String TAG = InitialSetupActivity.class.getSimpleName();
  private TextView headlineTextView;
  private ImageButton startScanButton;
  private EditText manualSetupKey;
  private EditText manualSetupSecretKey;
  private Button manualSetupButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.initial_setup_activity);

    headlineTextView = (TextView)findViewById(R.id.initial_setup_activity_headline);
    startScanButton = (ImageButton)findViewById(R.id.initial_setup_activity_start_scan);
    startScanButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        IntentIntegrator integrator = new IntentIntegrator(InitialSetupActivity.this);
        integrator.initiateScan();
      }
    });
    manualSetupKey = (EditText)findViewById(R.id.initial_setup_activity_manual_key);
    manualSetupSecretKey = (EditText)findViewById(R.id.initial_setup_activity_manual_secretkey);
    manualSetupButton = (Button)findViewById(R.id.initial_setup_activity_manual_setupbutton);
    manualSetupButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String key = manualSetupKey.getText().toString();
        String secretKey = manualSetupSecretKey.getText().toString();
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(secretKey)) {
          testAndSaveAccessKeys(key, secretKey);
        }
        //@TODO: REMOVE TESTING ONLY!!!
        else{
          key = "75f65d26-dbfa-4acc-9f00-d9be5d78907c";
          secretKey = "wCDgB1vWG9na7SuiqIikCOG3TFb1Q0r66Kt64w0TL7LKCJVJ9klpQZH266hibEDrCPmLzscPJwSqvQuG74/D1A==";
          testAndSaveAccessKeys(key, secretKey);
        }
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      //@TODO: handle scan result
      headlineTextView.setText(scanResult.getContents());
      //testAndSaveAccessKeys(key, secretKey);
    }
    super.onActivityResult(requestCode, resultCode, intent);
  }

  private void testAndSaveAccessKeys(String key, String secretKey) {
    // Test connection
    TestConnectionTask tct = new TestConnectionTask();
    tct.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, key, secretKey);
  }

  private class TestConnectionTask extends ICSAsyncTask<String, Void, Exception> {

    private String key;
    private String secretKey;
    private AccountInfo accountInfo;
    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {
      dialog = new ProgressDialog(InitialSetupActivity.this);
      dialog.setMessage(getString(R.string.initial_setup_testing));
      dialog.show();
    }

    @Override
    protected Exception doInBackground(String... params) {
      Log.d(TAG, "testing connection...");
      try {
        key = params[0];
        secretKey = params[1];
        ExchangeSpecification exchangeSpec = new ExchangeSpecification(MtGoxExchange.class);
        exchangeSpec.setApiKey(key);
        exchangeSpec.setSecretKey(secretKey);
        exchangeSpec.setSslUri(Constants.MTGOX_SSL_URI);
        exchangeSpec.setPlainTextUriStreaming(Constants.MTGOX_PLAIN_WEBSOCKET_URI);
        exchangeSpec.setSslUriStreaming(Constants.MTGOX_SSL_WEBSOCKET_URI);
        Exchange exchange = (MtGoxExchange)ExchangeFactory.INSTANCE.createExchange(exchangeSpec);
        accountInfo = exchange.getPollingAccountService().getAccountInfo();
      }
      catch (Exception e) {
        Log.i(TAG, "Exception", e);
        return e;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Exception exception) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      if (exception == null) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBitcoinTraderApplication());
        Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_KEY_MTGOX_APIKEY, key);
        editor.putString(Constants.PREFS_KEY_MTGOX_SECRETKEY, secretKey);
        editor.apply();
        Toast.makeText(InitialSetupActivity.this,
                InitialSetupActivity.this.getString(R.string.initial_setup_success, accountInfo.getUsername()), Toast.LENGTH_LONG).show();
        InitialSetupActivity.this.finish();
      }
      else {
        if (exception instanceof HttpException) {
          Toast.makeText(InitialSetupActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
        }
        else if (exception instanceof ExchangeException) {
          Toast.makeText(InitialSetupActivity.this, R.string.initial_setup_failed_wrong_credentials, Toast.LENGTH_LONG).show();
        }
      }
    }
  };
}
