//$URL$
//$Id$
/*
 * Copyright 2013 the original author or authors.
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
package de.schildbach.wallet.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.util.CrashReporter;
import java.lang.reflect.Method;

/**
 * @author Andreas Schildbach
 */
public abstract class ReportIssueDialogBuilder extends AlertDialog.Builder implements OnClickListener {
  
  private final Context context;
  private EditText viewDescription;
  private CheckBox viewCollectDeviceInfo;
  private CheckBox viewCollectApplicationLog;
  
  public ReportIssueDialogBuilder(final Context context, final int titleResId, final int messageResId) {
    super(context);
    
    this.context = context;
    this.setCancelable(false);
    final LayoutInflater inflater = LayoutInflater.from(context);
    final View view = inflater.inflate(R.layout.report_issue_dialog, null);
    
    ((TextView) view.findViewById(R.id.report_issue_dialog_message)).setText(messageResId);
    
    viewDescription = (EditText) view.findViewById(R.id.report_issue_dialog_description);
    
    viewCollectDeviceInfo = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_device_info);
    viewCollectApplicationLog = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_application_log);
    viewCollectApplicationLog.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? View.VISIBLE : View.GONE);
    
    setInverseBackgroundForced(true);
    setTitle(titleResId);
    setView(view);
    setPositiveButton(R.string.report_issue_dialog_report, this);
    setNegativeButton(R.string.button_cancel, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        onReportFinished();
      }
    });
  }
  
  public void onClick(final DialogInterface dialog, final int which) {
    
    
    final StringBuilder text = new StringBuilder();
    final ArrayList<Uri> attachments = new ArrayList<Uri>();
    
    text.append(viewDescription.getText()).append('\n');
    
    try {
      final CharSequence applicationInfo = collectApplicationInfo();
      
      text.append("\n\n\n=== application info ===\n\n");
      text.append(applicationInfo);
    } catch (final IOException x) {
      text.append("\n\n\n=== application info ===\n\n");
      text.append(x.toString()).append('\n');
    }
    
    try {
      final CharSequence stackTrace = collectStackTrace();
      
      if (stackTrace != null) {
        text.append("\n\n\n=== stack trace ===\n\n");
        text.append(stackTrace);
      }
    } catch (final IOException x) {
      text.append("\n\n\n=== stack trace ===\n\n");
      text.append(x.toString()).append('\n');
    }
    
    if (viewCollectDeviceInfo.isChecked()) {
      try {
        final CharSequence deviceInfo = collectDeviceInfo();
        
        text.append("\n\n\n=== device info ===\n\n");
        text.append(deviceInfo);
      } catch (final IOException x) {
        text.append("\n\n\n=== device info ===\n\n");
        text.append(x.toString()).append('\n');
      }
    }
    
    if (viewCollectApplicationLog.isChecked()) {
      try {
        final CharSequence applicationLog = collectApplicationLog();
        
        if (applicationLog != null) {
          final File file = File.createTempFile("application-log", null, context.getCacheDir());
          
          final FileWriter writer = new FileWriter(file);
          writer.write(applicationLog.toString());
          writer.close();
          
          chmod(file, 0777);
          
          attachments.add(Uri.fromFile(file));
        }
      } catch (final IOException x) {
        x.printStackTrace();
      }
    }
    
    if (CrashReporter.hasSavedBackgroundTraces()) {
      text.append("\n\n\n=== saved exceptions ===\n\n");
      
      try {
        CrashReporter.appendSavedBackgroundTraces(text);
      } catch (final IOException x) {
        text.append(x.toString()).append('\n');
      }
    }
    
    text.append("\n\nPUT ADDITIONAL COMMENTS TO THE TOP. DOWN HERE NOBODY WILL NOTICE.");
    
    startSend(subject(), text, attachments);
  }
  
  private void startSend(final CharSequence subject, final CharSequence text, final ArrayList<Uri> attachments) {
    final Intent intent;
    
    if (attachments.size() == 0) {
      intent = new Intent(Intent.ACTION_SEND);
      intent.setType("message/rfc822");
    } else if (attachments.size() == 1) {
      intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_STREAM, attachments.get(0));
    } else {
      intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
      intent.setType("text/plain");
      intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
    }
    
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.REPORT_EMAIL});
    if (subject != null) {
      intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    }
    intent.putExtra(Intent.EXTRA_TEXT, text);
    
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.report_issue_dialog_mail_intent_chooser)));
    onReportFinished();
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void chmod(final File path, final int mode) {
    try {
      final Class fileUtils = Class.forName("android.os.FileUtils");
      final Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
      setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
    } catch (final Exception x) {
      x.printStackTrace();
    }
  }
  
  protected abstract CharSequence subject();
  
  protected abstract CharSequence collectApplicationInfo() throws IOException;
  
  protected abstract CharSequence collectStackTrace() throws IOException;
  
  protected abstract CharSequence collectDeviceInfo() throws IOException;
  
  protected abstract CharSequence collectApplicationLog() throws IOException;
  
  protected abstract void onReportFinished();
}
