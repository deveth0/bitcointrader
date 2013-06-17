/*
 * Copyright 2011-2013 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.dev.eth0.bitcointrader.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.dev.eth0.bitcointrader.Constants;
import java.util.Calendar;

/**
 * @author Andreas Schildbach
 */
public class AutosyncReceiver extends BroadcastReceiver {

  private static final String TAG = AutosyncReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    AlarmManager service = (AlarmManager) context
            .getSystemService(Context.ALARM_SERVICE);
    Intent i = new Intent(context, new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MtGoxConnectionService.class);
        context.startService(service);
      }
    }.getClass());
    
    PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
            PendingIntent.FLAG_CANCEL_CURRENT);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    int repeatTime = prefs.getInt(Constants.PREFS_KEY_GENERAL_UPDATE, 0);
    if (repeatTime > 0) {
      Calendar cal = Calendar.getInstance();
      // Start 30 seconds after boot completed
      cal.add(Calendar.SECOND, 30);
      //
      // Fetch every 30 seconds
      // InexactRepeating allows Android to optimize the energy consumption
      service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
              cal.getTimeInMillis(), repeatTime, pending);

    }
  }
}
