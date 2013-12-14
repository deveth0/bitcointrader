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
import java.util.List;

/**
 *
 * @author deveth0
 */
public class ExchangeConfigurationDAO {

  private static final String TAG = ExchangeConfigurationDAO.class.getName();
  private ObjectMapper mMapper;
  private final Application mApplication;

  public ExchangeConfigurationDAO(Application pApplication) {
    mApplication = pApplication;
  }

  /**
   * Returns a list with all exchangeconfigurations. if Demo mode is activated, this returns a list with the demo account
   *
   * @throws ExchangeConfigurationException
   * @return
   */
  public List<ExchangeConfiguration> getExchangeConfigurations() throws ExchangeConfigurationException {
    List<ExchangeConfiguration> list;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplication);
    if (prefs.getBoolean(Constants.PREFS_KEY_DEMO, false)) {
      list = new ArrayList<ExchangeConfiguration>();
      list.add(new ExchangeConfiguration(null, null, null, null, null, true, ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.DEMO));
      return list;
    }
    try {
      FileInputStream fis = mApplication.openFileInput("exchangeConfigurationTest");
      list = getObjectMapper().readValue(fis, new TypeReference<List<ExchangeConfiguration>>() {
      });
      return list;
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

  public void setExchangeConfigurationPrimary(String exchangeConfigurationId) throws ExchangeConfigurationException {
    List<ExchangeConfiguration> configs = getExchangeConfigurations();
    for (ExchangeConfiguration config : configs) {
      config.setPrimary(TextUtils.equals(config.getId(), exchangeConfigurationId));
    }
    writeExchangeConfiguration(configs);
  }

  private void writeExchangeConfiguration(List<ExchangeConfiguration> configs) throws ExchangeConfigurationException {
    try {
      FileOutputStream fos = mApplication.openFileOutput("exchangeConfigurationTest", Context.MODE_PRIVATE);
      getObjectMapper().writeValue(fos, configs);
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
