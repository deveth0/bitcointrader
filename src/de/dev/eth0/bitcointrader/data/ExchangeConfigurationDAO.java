//$URL$
//$Id$
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dev.eth0.bitcointrader.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
   * @return
   */
  public List<ExchangeConfiguration> getExchangeConfigurations() throws ExchangeConfigurationException {
    List<ExchangeConfiguration> list;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplication);
    String mtGoxAPIKey, mtGoxSecretKey;
    if (prefs.getBoolean(Constants.PREFS_KEY_DEMO, false)) {
      mtGoxAPIKey = Constants.MTGOX_DEMO_ACCOUNT_APIKEY;
      mtGoxSecretKey = Constants.MTGOX_DEMO_ACCOUNT_SECRETKEY;
      ExchangeConfiguration exchangeConfig = new ExchangeConfiguration(
              "mtgox", null, mtGoxAPIKey, mtGoxSecretKey, ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.MTGOX);
      list = new ArrayList<ExchangeConfiguration>();
      list.add(exchangeConfig);
      return list;
    }
    try {
      FileInputStream fis = mApplication.openFileInput("exchangeConfigurationTest");
      list = getObjectMapper().readValue(fis, new TypeReference<List<ExchangeConfiguration>>() {
      });
      return list;
    } catch (FileNotFoundException ex) {
      Log.w(TAG, "Could not read exchange configurations", ex);
      throw new ExchangeConfigurationException(ex);
    } catch (IOException ioe) {
      Log.w(TAG, Log.getStackTraceString(ioe), ioe);
      throw new ExchangeConfigurationException(ioe);
    }
  }

  private ObjectMapper getObjectMapper() {
    if (mMapper == null) {
      mMapper = new ObjectMapper();
    }
    return mMapper;
  }

  public void removeExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) throws ExchangeConfigurationException {
    List<ExchangeConfiguration> configs = getExchangeConfigurations();
    configs.remove(exchangeConfiguration);
    writeExchangeConfiguration(configs);
  }

  public void addExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) throws ExchangeConfigurationException {
    List<ExchangeConfiguration> configs = getExchangeConfigurations();
    configs.add(exchangeConfiguration);
    writeExchangeConfiguration(configs);
  }

  private void writeExchangeConfiguration(List<ExchangeConfiguration> configs) throws ExchangeConfigurationException {
    try {
      FileOutputStream fos = mApplication.openFileOutput("exchangeConfigurationTest", Context.MODE_PRIVATE);
      getObjectMapper().writeValue(fos, configs);
    } catch (IOException ioe) {
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
