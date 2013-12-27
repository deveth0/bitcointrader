//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dev.eth0.bitcointrader.Constants;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author deveth0
 */
public class ExchangeConfigurationDAO {

  private static final String TAG = ExchangeConfigurationDAO.class.getName();
  private ObjectMapper mMapper;
  private final Application mApplication;
  private static Map<String, ExchangeConfiguration> exchangeConfigs = null;
  private static Map<String, ExchangeConfiguration> activeExchangeConfigs = null;

  public ExchangeConfigurationDAO(Application pApplication) {
    mApplication = pApplication;
  }

  /**
   * Returns a list with all active configurations
   *
   * @return
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public Map<String, ExchangeConfiguration> getActiveExchangeConfigurations() throws ExchangeConfigurationException {
    if (activeExchangeConfigs == null) {
      Map<String, ExchangeConfiguration> configs = getExchangeConfigurations();
      activeExchangeConfigs = new TreeMap<String, ExchangeConfiguration>();
      for (Entry<String, ExchangeConfiguration> config : configs.entrySet()) {
        if (config.getValue().isEnabled()) {
          activeExchangeConfigs.put(config.getKey(), config.getValue());
        }
      }
    }
    return activeExchangeConfigs;
  }

  /**
   * Returns a list with all exchangeconfigurations. if Demo mode is activated, this returns a list with the demo account
   *
   * @throws ExchangeConfigurationException
   * @return
   */
  public Map<String, ExchangeConfiguration> getExchangeConfigurations() throws ExchangeConfigurationException {
    if (exchangeConfigs == null) {
      Log.d(TAG, "Reading exchange configurations from file");
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplication);
      exchangeConfigs = new TreeMap<String, ExchangeConfiguration>();
      if (prefs.getBoolean(Constants.PREFS_KEY_DEMO, false)) {
        exchangeConfigs.put("demo", new ExchangeConfiguration(null, null, null, null, null, true, true, ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.DEMO, null));
      }
      else {
        try {
          FileInputStream fis = mApplication.openFileInput("exchangeConfigurationTest");
          List<ExchangeConfiguration> configs = getObjectMapper().readValue(fis, new TypeReference<List<ExchangeConfiguration>>() {
          });
          for (ExchangeConfiguration conf : configs) {
            exchangeConfigs.put(conf.getId(), conf);
          }
        }
        catch (FileNotFoundException ex) {
          Log.w(TAG, "Could not read exchange configurations", ex);
          throw new ExchangeConfigurationException(ex);
        }
        catch (IOException ioe) {
          Log.w(TAG, Log.getStackTraceString(ioe), ioe);
          throw new ExchangeConfigurationException(ioe);
        }
      }
    }
    return exchangeConfigs;
  }

  /**
   * returns the primary exchange (or the first active one)
   *
   * @return
   */
  public ExchangeConfiguration getPrimaryExchangeConfiguration() {
    Map<String, ExchangeConfiguration> configs;
    try {
      configs = getActiveExchangeConfigurations();
      for (Entry<String, ExchangeConfiguration> config : configs.entrySet()) {
        if (config.getValue().isPrimary()) {
          return config.getValue();
        }
      }
      if (configs.size() > 0) {
        return configs.values().iterator().next();
      }
    }
    catch (ExchangeConfigurationException ex) {
      Log.w(TAG, Log.getStackTraceString(ex), ex);
    }
    return null;
  }

  public ExchangeConfiguration getExchangeConfiguration(String id) {
    if (!TextUtils.isEmpty(id)) {
      try {
        Map<String, ExchangeConfiguration> configs = getExchangeConfigurations();
        return configs.get(id);
      }
      catch (ExchangeConfigurationException ex) {
        Log.w(TAG, Log.getStackTraceString(ex), ex);
      }
    }
    return null;
  }

  private ObjectMapper getObjectMapper() {
    if (mMapper == null) {
      mMapper = new ObjectMapper();
    }
    return mMapper;
  }

  /**
   * Removes the given exchangeConfiguration from the exchangeConfigurationFile
   *
   * @param exchangeConfiguration
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public void removeExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) throws ExchangeConfigurationException {
    Map<String, ExchangeConfiguration> configs = getExchangeConfigurations();
    configs.remove(exchangeConfiguration.getId());
    writeExchangeConfiguration(configs);
  }

  /**
   * Writes the given exchangeConfiguration into the exchangeConfigurationFile
   *
   * @param exchangeConfiguration
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public void addExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) throws ExchangeConfigurationException {
    Map<String, ExchangeConfiguration> configs = getExchangeConfigurations();
    // take care, it's not added twice
    configs.put(exchangeConfiguration.getId(), exchangeConfiguration);
    writeExchangeConfiguration(configs);
  }

  /**
   * Sets the given exchangeconfig as primary (and disables the primary tag for all other exchanges)
   *
   * @param exchangeConfigurationId
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public void setExchangeConfigurationPrimary(String exchangeConfigurationId) throws ExchangeConfigurationException {
    Map<String, ExchangeConfiguration> configs = getExchangeConfigurations();
    for (ExchangeConfiguration config : configs.values()) {
      config.setPrimary(TextUtils.equals(config.getId(), exchangeConfigurationId));
    }
    writeExchangeConfiguration(configs);
  }

  /**
   * Toogles the enabled state of the exchangeconfiguration
   *
   * @param id
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public void toogleExchangeConfigurationEnabled(String id) throws ExchangeConfigurationException {
    ExchangeConfiguration config = getExchangeConfiguration(id);
    if (config != null) {
      config.setEnabled(!config.isEnabled());
      updateExchangeConfig(config);
    }
  }

  public void setTrailingStopLossConfiguration(ExchangeConfiguration exchangeConfig, ExchangeConfiguration.TrailingStopLossConfiguration trailingconfig) throws ExchangeConfigurationException {
    exchangeConfig.setTrailingStopLossConfig(trailingconfig);
    updateExchangeConfig(exchangeConfig);
  }

  private void updateExchangeConfig(ExchangeConfiguration exchangeConfig) throws ExchangeConfigurationException {
    Map<String, ExchangeConfiguration> configs = getExchangeConfigurations();
    configs.put(exchangeConfig.getId(), exchangeConfig);
    writeExchangeConfiguration(configs);
    LocalBroadcastManager.getInstance(mApplication).sendBroadcast(new Intent(Constants.EXCHANGE_CHANGED).putExtra(Constants.EXTRA_EXCHANGE, exchangeConfig.getId()));
  }

  private void writeExchangeConfiguration(Map<String, ExchangeConfiguration> configs) throws ExchangeConfigurationException {
    // if only one exchange is available, this has to be the primary one
    if (configs.size() == 1) {
      configs.values().iterator().next().setPrimary(true);
    }
    List<ExchangeConfiguration> list = new ArrayList<ExchangeConfiguration>(configs.values());
    Collections.sort(list, new Comparator<ExchangeConfiguration>() {
      public int compare(ExchangeConfiguration lhs, ExchangeConfiguration rhs) {
        return Boolean.compare(rhs.isPrimary(), lhs.isPrimary());
      }
    });
    try {
      FileOutputStream fos = mApplication.openFileOutput("exchangeConfigurationTest", Context.MODE_PRIVATE);
      getObjectMapper().writeValue(fos, list);
      // reset cache
      exchangeConfigs = null;
      activeExchangeConfigs = null;
      LocalBroadcastManager.getInstance(mApplication).sendBroadcast(new Intent(Constants.EXCHANGE_CHANGED));
    }
    catch (IOException ioe) {
      Log.w(TAG, Log.getStackTraceString(ioe), ioe);
      throw new ExchangeConfigurationException(ioe);
    }
  }

  public static class ExchangeConfigurationException extends Exception {

    public ExchangeConfigurationException(Throwable throwable) {
      super(throwable);
    }
  }
}
