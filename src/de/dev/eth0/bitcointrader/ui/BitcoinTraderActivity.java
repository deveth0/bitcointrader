//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.TextView;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.dto.trade.Wallet;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO;
import de.dev.eth0.bitcointrader.ui.fragments.listAdapter.ExchangeConfigurationListAdapter;
import de.schildbach.wallet.integration.android.BitcoinIntegration;
import de.schildbach.wallet.ui.HelpDialogFragment;

/**
 * @author Alexander Muthmann
 */
public class BitcoinTraderActivity extends AbstractBitcoinTraderActivity {

  private static final String TAG = BitcoinTraderActivity.class.getName();
  private TextView titleView;
  private Menu mSelectCurrencyItem;
  private ListView mDrawerList;
  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private CharSequence mDrawerTitle;
  private ExchangeConfigurationListAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.bitcointrader_content);


    mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
    mDrawerList = (ListView)findViewById(R.id.bitcointrader_exchange_drawer);
    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mDrawerTitle = getString(R.string.exchange_drawer_title);
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
            R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
      /**
       * Called when a drawer has settled in a completely closed state.
       */
      @Override
      public void onDrawerClosed(View view) {
        getSupportActionBar().setTitle(getExchangeService().getExchangeName());
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      /**
       * Called when a drawer has settled in a completely open state.
       */
      @Override
      public void onDrawerOpened(View drawerView) {
        getSupportActionBar().setTitle(mDrawerTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };

    // Set the drawer toggle as the DrawerListener
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    adapter = new ExchangeConfigurationListAdapter(this);
    mDrawerList.setAdapter(adapter);
    mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ExchangeConfiguration config = adapter.getItem(position);
        if (config != null) {
          getExchangeService().setExchange(config);
          mDrawerLayout.closeDrawer(mDrawerList);
        }
      }
    });
    updateExchangeDrawer();
    init();



    getSupportActionBar().setTitle(getExchangeService().getExchangeName());
  }

  private void init() {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    int currentVersionNumber = getBitcoinTraderApplication().applicationVersionCode();

    int savedVersionNumber = sharedPref.getInt(Constants.PREFS_KEY_LAST_VERSION_KEY, 0);

    if (currentVersionNumber > savedVersionNumber) {
      showWhatsNewDialog();

      Editor editor = sharedPref.edit();

      editor.putInt(Constants.PREFS_KEY_LAST_VERSION_KEY, currentVersionNumber);
      editor.commit();
    }
  }

  private void showWhatsNewDialog() {
    LayoutInflater inflater = LayoutInflater.from(this);

    View view = inflater.inflate(R.layout.dialog_whatsnew, null);

    ((TextView)view.findViewById(R.id.dialog_whatsnew_version)).setText(getBitcoinTraderApplication().applicationVersionName());
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setView(view).setTitle(R.string.whats_new_title)
            .setPositiveButton(R.string.whats_new_ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    AlertDialog dialog = builder.create();
    dialog.show();
  }

  private void updateExchangeDrawer() {
    try {
      adapter.replace(getBitcoinTraderApplication().getExchangeConfigurationDAO().getExchangeConfigurations());
    }
    catch (ExchangeConfigurationDAO.ExchangeConfigurationException ece) {
      Log.w(TAG, "Could not load exchange configurations", ece);
    }
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onResume() {
    getBitcoinTraderApplication().startExchangeService();
    updateExchangeDrawer();
    super.onResume();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getSupportMenuInflater().inflate(R.menu.bitcointrader_options, menu);

    mSelectCurrencyItem = menu.findItem(R.id.bitcointrader_options_select_currency).getSubMenu();
    mSelectCurrencyItem.clear();
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // If the nav drawer is open, hide action items related to the content view
    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
    menu.findItem(R.id.bitcointrader_options_select_currency).setVisible(!drawerOpen);
    menu.findItem(R.id.bitcointrader_options_refresh).setVisible(!drawerOpen);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
          mDrawerLayout.closeDrawer(mDrawerList);
        }
        else {
          mDrawerLayout.openDrawer(mDrawerList);
        }
        return true;
      case R.id.bitcointrader_options_select_currency:
        showSelectCurrencyPopup();
        break;
      case R.id.bitcointrader_options_price_chart:
        startActivity(new Intent(this, PriceChartActivity.class));
        break;
      case R.id.bitcointrader_options_wallet_history:
        startActivity(new Intent(this, WalletHistoryActivity.class));
        break;
      case R.id.bitcointrader_options_market_depth:
        startActivity(new Intent(this, MarketDepthActivity.class));
        break;
      case R.id.bitcointrader_options_refresh:
        LocalBroadcastManager.getInstance(getBitcoinTraderApplication()).sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
        break;
      case R.id.bitcointrader_options_about:
        startActivity(new Intent(this, AboutActivity.class));
        break;
      case R.id.bitcointrader_options_preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      case R.id.bitcointrader_options_exchange_configuration:
        startActivity(new Intent(this, ExchangeConfigurationActivity.class));
        break;
      case R.id.bitcointrader_options_donate:
        BitcoinIntegration.request(this, Constants.DONATION_ADDRESS);
        break;
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(getSupportFragmentManager(), "help");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showSelectCurrencyPopup() {
    if (mSelectCurrencyItem.size() == 0) {
      int idx = 0;
      if (getExchangeService() != null && getExchangeService().getAccountInfo() != null) {
        for (Wallet wallet : getExchangeService().getAccountInfo().getWallets()) {
          if (wallet != null && wallet.getBalance() != null
                  && !TextUtils.isEmpty(wallet.getCurrency())
                  && !wallet.getCurrency().equals(Constants.CURRENCY_CODE_BITCOIN)) {
            MenuItem mi = mSelectCurrencyItem.add(Menu.NONE, idx++, Menu.NONE, wallet.getCurrency());
            mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem item) {
                getExchangeService().setCurrency(item.getTitle().toString());
                LocalBroadcastManager.getInstance(getBitcoinTraderApplication()).sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
                return true;
              }
            });
          }
        }
      }
    }
  }
}
