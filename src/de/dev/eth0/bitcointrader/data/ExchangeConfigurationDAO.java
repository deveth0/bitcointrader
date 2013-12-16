//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

/**
 *
 * @author deveth0
 */
public class ExchangeConfigurationDAO {

  private static final String TAG = ExchangeConfigurationDAO.class.getName();
  private ObjectMapper mMapper;
  private final Application mApplication;
  private static List<ExchangeConfiguration> exchangeConfigs = null;
  private static List<ExchangeConfiguration> activeExchangeConfigs = null;

  public ExchangeConfigurationDAO(Application pApplication) {
    mApplication = pApplication;
  }

  /**
   * Returns a list with all active configurations
   *
   * @return
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public List<ExchangeConfiguration> getActiveExchangeConfigurations() throws ExchangeConfigurationException {
    if (activeExchangeConfigs == null) {
      List<ExchangeConfiguration> configs = getExchangeConfigurations();
      activeExchangeConfigs = new ArrayList<ExchangeConfiguration>();
      for (ExchangeConfiguration config : configs) {
        if (config.isEnabled()) {
          activeExchangeConfigs.add(config);
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
  public List<ExchangeConfiguration> getExchangeConfigurations() throws ExchangeConfigurationException {
    if (exchangeConfigs == null) {
      Log.d(TAG, "Reading exchange configurations from file");
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplication);
      if (prefs.getBoolean(Constants.PREFS_KEY_DEMO, false)) {
        exchangeConfigs = new ArrayList<ExchangeConfiguration>();
        exchangeConfigs.add(new ExchangeConfiguration(null, null, null, null, null, true, true, ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.DEMO));
      }
      else {
        try {
          FileInputStream fis = mApplication.openFileInput("exchangeConfigurationTest");
          exchangeConfigs = getObjectMapper().readValue(fis, new TypeReference<List<ExchangeConfiguration>>() {
          });
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
    List<ExchangeConfiguration> configs;
    try {
      configs = getActiveExchangeConfigurations();
      for (ExchangeConfiguration config : configs) {
        if (config.isPrimary()) {
          return config;
        }
      }
      if (configs.size() > 0) {
        return configs.get(0);
      }
    }
    catch (ExchangeConfigurationException ex) {
      Log.w(TAG, Log.getStackTraceString(ex), ex);
    }
    return null;
  }

  public ExchangeConfiguration getExchangeConfiguration(String id) {
    List<ExchangeConfiguration> configs;
    try {
      configs = getExchangeConfigurations();
      for (ExchangeConfiguration config : configs) {
        if (TextUtils.equals(config.getId(), id)) {
          return config;
        }
      }
    }
    catch (ExchangeConfigurationException ex) {
      Log.w(TAG, Log.getStackTraceString(ex), ex);
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
    List<ExchangeConfiguration> configs = getExchangeConfigurations();
    configs.remove(exchangeConfiguration);
    writeExchangeConfiguration(configs);
  }

  /**
   * Writes the given exchangeConfiguration into the exchangeConfigurationFile
   *
   * @param exchangeConfiguration
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public void addExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) throws ExchangeConfigurationException {
    List<ExchangeConfiguration> configs = getExchangeConfigurations();
    // take care, it's not added twice
    configs.remove(exchangeConfiguration);
    configs.add(exchangeConfiguration);
    writeExchangeConfiguration(configs);
  }

  /**
   * Sets the given exchangeconfig as primary (and disables the primary tag for all other exchanges)
   *
   * @param exchangeConfigurationId
   * @throws de.dev.eth0.bitcointrader.data.ExchangeConfigurationDAO.ExchangeConfigurationException
   */
  public void setExchangeConfigurationPrimary(String exchangeConfigurationId) throws ExchangeConfigurationException {
    List<ExchangeConfiguration> configs = getExchangeConfigurations();
    for (ExchangeConfiguration config : configs) {
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
    List<ExchangeConfiguration> configs = getExchangeConfigurations();
    for (ExchangeConfiguration config : configs) {
      if (TextUtils.equals(config.getId(), id)) {
        config.setEnabled(!config.isEnabled());
      }
    }
    writeExchangeConfiguration(configs);
  }

  private void writeExchangeConfiguration(List<ExchangeConfiguration> configs) throws ExchangeConfigurationException {
    // if only one exchange is available, this has to be the primary one
    if (configs.size() == 1) {
      configs.get(0).setPrimary(true);
    }
    Collections.sort(configs, new Comparator<ExchangeConfiguration>() {
      public int compare(ExchangeConfiguration lhs, ExchangeConfiguration rhs) {
        return Boolean.compare(rhs.isPrimary(), lhs.isPrimary());
      }
    });
    try {
      FileOutputStream fos = mApplication.openFileOutput("exchangeConfigurationTest", Context.MODE_PRIVATE);
      getObjectMapper().writeValue(fos, configs);
      // reset cache
      exchangeConfigs = null;
      activeExchangeConfigs = null;
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
