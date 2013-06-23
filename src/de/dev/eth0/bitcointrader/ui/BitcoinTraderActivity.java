package de.dev.eth0.bitcointrader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.util.CrashReporter;
import de.schildbach.wallet.integration.android.BitcoinIntegration;
import java.io.IOException;

public final class BitcoinTraderActivity extends AbstractBitcoinTraderActivity {

  private LocalBroadcastManager broadcastManager;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.bitcointrader_content);
    broadcastManager = LocalBroadcastManager.getInstance(getBitcoinTraderApplication());
    if (savedInstanceState == null) {
      checkAlerts();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    getBitcoinTraderApplication().startExchangeService();
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    super.onCreateOptionsMenu(menu);
    getSupportMenuInflater().inflate(R.menu.bitcointrader_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        broadcastManager.sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
        break;
      case R.id.bitcointrader_options_about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
      case R.id.bitcointrader_options_preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        return true;
      case R.id.bitcointrader_options_donate:
        BitcoinIntegration.request(this, Constants.DONATION_ADDRESS);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void checkAlerts() {

    if (CrashReporter.hasSavedCrashTrace()) {
      final StringBuilder stackTrace = new StringBuilder();
      final StringBuilder applicationLog = new StringBuilder();

      try {
        CrashReporter.appendSavedCrashTrace(stackTrace);
        CrashReporter.appendSavedCrashApplicationLog(applicationLog);
      }
      catch (final IOException x) {
        x.printStackTrace();
      }

      final ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(this, R.string.report_issue_dialog_title_crash,
              R.string.report_issue_dialog_message_crash) {
        @Override
        protected CharSequence subject() {
          return Constants.REPORT_SUBJECT_CRASH + " " + getBitcoinTraderApplication().applicationVersionName();
        }

        @Override
        protected CharSequence collectApplicationInfo() throws IOException {
          final StringBuilder applicationInfo = new StringBuilder();
          CrashReporter.appendApplicationInfo(applicationInfo, getBitcoinTraderApplication());
          return applicationInfo;
        }

        @Override
        protected CharSequence collectStackTrace() throws IOException {
          if (stackTrace.length() > 0) {
            return stackTrace;
          }
          else {
            return null;
          }
        }

        @Override
        protected CharSequence collectDeviceInfo() throws IOException {
          final StringBuilder deviceInfo = new StringBuilder();
          CrashReporter.appendDeviceInfo(deviceInfo, BitcoinTraderActivity.this);
          return deviceInfo;
        }

        @Override
        protected CharSequence collectApplicationLog() throws IOException {
          if (applicationLog.length() > 0) {
            return applicationLog;
          }
          else {
            return null;
          }
        }
      };

      dialog.show();
    }
  }
}
