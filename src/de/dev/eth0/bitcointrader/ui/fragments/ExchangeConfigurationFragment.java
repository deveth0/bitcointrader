//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.Context;
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
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.fasterxml.jackson.core.type.TypeReference;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.AddExchangeConfigurationActivity;
import de.dev.eth0.bitcointrader.ui.fragments.listAdapter.ExchangeConfigurationListAdapter;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    ListView lv = (ListView) layout.findViewById(android.R.id.list);
    ViewGroup parent = (ViewGroup) lv.getParent();

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
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = (BitcoinTraderApplication) activity.getApplication();
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
      case R.id.exchange_configuration_add:
        startActivity(new Intent(activity, AddExchangeConfigurationActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.exchangeconfiguration_options, menu);
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {
    switch (item.getItemId()) {
      case R.id.exchange_configuration_context_delete:
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        ExchangeConfiguration selectedConfig = adapter.getItem(info.position);
        deleteExchangeConfiguration(selectedConfig);
        return true;
      case R.id.exchange_configuration_context_edit:
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
      FileInputStream fis = application.openFileInput("exchangeConfigurationTest");
      List<ExchangeConfiguration> list = application.getObjectMapper().readValue(fis, new TypeReference<List<ExchangeConfiguration>>() {
      });
      adapter.replace(list);
    } catch (FileNotFoundException ex) {
      Log.e(TAG, "FileNotFoundException", ex);
    } catch (IOException ioe) {
      Log.e(TAG, "IOException", ioe);
    }
  }

  private void deleteExchangeConfiguration(ExchangeConfiguration selectedConfig) {
    List<ExchangeConfiguration> configs = new ArrayList<ExchangeConfiguration>(adapter.getEntries());
    configs.remove(selectedConfig);
    writeOutExchangeConfigurations(configs);
  }

  private void writeOutExchangeConfigurations(List<ExchangeConfiguration> configs) {
    try {
      FileOutputStream fos = application.openFileOutput("exchangeConfigurationTest", Context.MODE_PRIVATE);
      application.getObjectMapper().writeValue(fos, configs);
      adapter.replace(configs);
    } catch (FileNotFoundException ex) {
      Log.e(TAG, "FileNotFoundException", ex);
    } catch (IOException ioe) {
      Log.e(TAG, "IOException", ioe);
    }
  }

}
