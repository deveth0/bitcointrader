//$URL$
//$Id$
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dev.eth0.bitcointrader.exchanges;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.exchanges.wrappers.BitstampExchangeWrapper;
import de.dev.eth0.bitcointrader.exchanges.wrappers.DemoExchangeWrapper;
import de.dev.eth0.bitcointrader.exchanges.wrappers.MtGoxExchangeWrapper;

/**
 *
 * @author deveth0
 */
public class ExchangeWrapperFactory {

  public static ExchangeWrapper forExchangeConfiguration(ExchangeConfiguration config) {
    if (config.getConnectionSettings() == ExchangeConfiguration.EXCHANGE_CONNECTION_SETTING.DEMO) {
      return new DemoExchangeWrapper();
    }
    Exchange exchange = ExchangeFactory.INSTANCE.createExchange(config.getConnectionSettings().getExchangeClassName());
    ExchangeSpecification exchangeSpec = exchange.getDefaultExchangeSpecification();
    exchangeSpec.setApiKey(config.getApiKey());
    exchangeSpec.setSecretKey(config.getSecretKey());
    exchangeSpec.setUserName(config.getUserName());
    exchange.applySpecification(exchangeSpec);

    switch (config.getConnectionSettings()) {
      case MTGOX:
        return new MtGoxExchangeWrapper(config.getName(), exchange);
      case BITSTAMP:
        return new BitstampExchangeWrapper(config.getName(), exchange);
      case BTCN:
    }
    return null;
  }

  private ExchangeWrapperFactory() {

  }
}
