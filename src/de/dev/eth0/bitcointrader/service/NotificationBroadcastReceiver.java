/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dev.eth0.bitcointrader.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.BitcoinTraderActivity;

/**
 *
 * @author deveth0
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

  private final static int ORDER_EXECUTED_NOTIFICATION_ID = 1;
  private final static int UPDATE_FAILED_NOTIFICATION_ID = 2;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Constants.UPDATE_FAILED)) {
      notifyUpdateFailed(context);
    }
    else if (intent.getAction().equals(Constants.UPDATE_SUCCEDED)) {
      NotificationManager notificationmanager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationmanager.cancel(UPDATE_FAILED_NOTIFICATION_ID);
    }
    else if (intent.getAction().equals(Constants.ORDER_EXECUTED)) {
      // get orders
      //intent.get
      notifyOrderExecuted(context, intent.getStringArrayExtra(Constants.EXTRA_ORDERS));
    }
  }

  private void notifyUpdateFailed(Context context) {
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_action_warning)
            .setContentTitle(context.getString(R.string.notify_update_failed_title))
            .setContentText(context.getString(R.string.notify_update_failed_text));
    Intent resultIntent = new Intent(context, BitcoinTraderActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(BitcoinTraderActivity.class);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    mBuilder.setAutoCancel(true);
    NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(UPDATE_FAILED_NOTIFICATION_ID, mBuilder.build());
  }

  private void notifyOrderExecuted(Context context, String[] executedOrders) {
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_action_bitcoin)
            .setContentTitle(context.getString(R.string.notify_order_executed_title))
            .setContentText(context.getString(R.string.notify_order_executed_text));
    NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
    notificationStyle.setBigContentTitle(context.getString(R.string.notify_order_executed_text));
    for (String lo : executedOrders) {
      notificationStyle.bigText(lo);
    }
    mBuilder.setStyle(notificationStyle);
    Intent resultIntent = new Intent(context, BitcoinTraderActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(BitcoinTraderActivity.class);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    mBuilder.setAutoCancel(true);
    NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(ORDER_EXECUTED_NOTIFICATION_ID, mBuilder.build());
  }
}
