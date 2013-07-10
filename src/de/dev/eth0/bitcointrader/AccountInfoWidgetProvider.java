//$URL$
//$Id$
package de.dev.eth0.bitcointrader;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.xeiam.xchange.mtgox.v2.dto.account.polling.Wallets;
import de.dev.eth0.bitcointrader.service.ExchangeService;

/**
 * @author Alexander Muthmann
 */
public class AccountInfoWidgetProvider extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    BitcoinTraderApplication application = (BitcoinTraderApplication)context.getApplicationContext();
    ExchangeService exchangeService = application.getExchangeService();

    updateWidgets(context, appWidgetManager, appWidgetIds, exchangeService);
  }

  private void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, ExchangeService exchangeService) {
    if (exchangeService != null && exchangeService.getAccountInfo() != null) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.account_info_widget_content);
      Wallets wallets = exchangeService.getAccountInfo().getWallets();
      if (wallets != null && wallets.getUSD() != null) {
        views.setTextViewText(R.id.account_info_widget_btc, wallets.getBTC().getBalance().toString());
        views.setTextViewText(R.id.account_info_widget_balance, wallets.getUSD().getBalance().toString());
        for (int appWidgetId : appWidgetIds) {
          appWidgetManager.updateAppWidget(appWidgetId, views);
        }
      }
    }
  }
}
