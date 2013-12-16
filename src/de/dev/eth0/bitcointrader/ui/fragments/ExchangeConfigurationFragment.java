//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.fragments.listAdapter.ExchangeConfigurationListAdapter;
import de.schildbach.wallet.ui.HelpDialogFragment;

/**
 * @author Alexander Muthmann
 */
public class ExchangeConfigurationFragment extends SherlockListFragment {

  private static final String TAG = ExchangeConfigurationFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private ExchangeConfigurationListAdapter adapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View layout = super.onCreateView(inflater, container, savedInstanceState);
    ListView lv = (ListView)layout.findViewById(android.R.id.list);
    ViewGroup parent = (ViewGroup)lv.getParent();

    // Remove ListView and add CustomView  in its place
    int lvIndex = parent.indexOfChild(lv);
    parent.removeViewAt(lvIndex);
    View view = inflater.inflate(R.layout.exchange_configuration_fragment, container, false);
    parent.addView(view, lvIndex, lv.getLayoutParams());
    return layout;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity)activity;
    this.application = (BitcoinTraderApplication)activity.getApplication();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    adapter = new ExchangeConfigurationListAdapter(activity);
    setListAdapter(adapter);
    setHasOptionsMenu(true);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    registerForContextMenu(getListView());
    int text = R.string.exchange_configuration_empty_text;
    SpannableStringBuilder emptyText = new SpannableStringBuilder(getString(text));
    emptyText.setSpan(new StyleSpan(Typeface.BOLD), 0, emptyText.length(), SpannableStringBuilder.SPAN_POINT_MARK);
    setEmptyText(emptyText);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    ExchangeConfiguration entry = adapter.getItem(position);
    if (entry != null) {
      application.getExchangeService().setExchange(entry);
    }
    super.onListItemClick(l, v, position, id);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_exchange_configuration");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.exchangeconfiguration_options, menu);

    Menu exchangeMenu = menu.findItem(R.id.exchange_configuration_add).getSubMenu();
    exchangeMenu.clear();
    int idx = 0;
    for (final ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING exchange : ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.values()) {
      if (exchange == ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.DEMO) {
        continue;
      }
      MenuItem mi = exchangeMenu.add(Menu.NONE, idx++, Menu.NONE, exchange.getDisplayName());
      mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
          startActivity(new Intent(activity, exchange.getSetupActivity()));
          return true;
        }
      });
    }
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    final ExchangeConfiguration selectedConfig = adapter.getItem(info.position);
    switch (item.getItemId()) {
      case R.id.exchange_configuration_context_delete:
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setPositiveButton(R.string.exchange_configuration_delete_confirm_ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              application.getExchangeConfigurationDAO().removeExchangeConfiguration(selectedConfig);
            }
            catch (ExchangeConfigurationDAO.ExchangeConfigurationException ece) {
              Log.w(TAG, Log.getStackTraceString(ece));
            }
            updateView();
          }
        });
        alertDialogBuilder.setNegativeButton(R.string.exchange_configuration_delete_confirm_cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        alertDialogBuilder.setMessage(R.string.exchange_configuration_delete_confirm_message);
        alertDialogBuilder.create().show();
        return true;
      case R.id.exchange_configuration_context_edit:
        startActivity(new Intent(activity, selectedConfig.getConnectionSettings().getSetupActivity()).putExtra(Constants.EXTRA_EXCHANGE, selectedConfig.getId()));
        return true;
      case R.id.exchange_configuration_context_set_primary:
        try {
          application.getExchangeConfigurationDAO().setExchangeConfigurationPrimary(selectedConfig.getId());
        }
        catch (ExchangeConfigurationDAO.ExchangeConfigurationException ece) {
          Log.w(TAG, Log.getStackTraceString(ece));
        }
        updateView();
        return true;
      case R.id.exchange_configuration_context_toogle_enabled:
        if (selectedConfig.isPrimary()) {
          Toast.makeText(getActivity(), R.string.exchange_configuration_toogle_primary_error, Toast.LENGTH_LONG).show();
        }
        else {
        try {
          application.getExchangeConfigurationDAO().toogleExchangeConfigurationEnabled(selectedConfig.getId());
        }
        catch (ExchangeConfigurationDAO.ExchangeConfigurationException ece) {
          Log.w(TAG, Log.getStackTraceString(ece));
        }
          updateView();
        }
        return true;
    }

    return super.onContextItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    getSherlockActivity().getMenuInflater().inflate(R.menu.exchangeconfiguration_context, menu);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView();
  }

  protected void updateView() {
    Log.d(TAG, ".updateView");
    try {
      adapter.replace(application.getExchangeConfigurationDAO().getExchangeConfigurations());
    }
    catch (ExchangeConfigurationDAO.ExchangeConfigurationException ece) {
      Log.w(TAG, Log.getStackTraceString(ece));
    }
  }
}
