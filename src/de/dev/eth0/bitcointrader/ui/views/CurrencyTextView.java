package de.dev.eth0.bitcointrader.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.Constants;
import org.joda.money.BigMoney;
import org.joda.money.format.MoneyAmountStyle;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;

public class CurrencyTextView extends TextView {
  
  private BigMoney amount = null;
  private String prefix = null;
  private boolean showcurrencyCode = true;
  private static final MoneyFormatter mfbNoCurrencyCode = new MoneyFormatterBuilder().
          appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter();
  private static final MoneyFormatter mfbCurrencyCode = new MoneyFormatterBuilder().appendCurrencyCode().appendLiteral(" ")
          .appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter();
  
  public CurrencyTextView(Context context) {
    super(context);
  }
  
  public CurrencyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public void setShowCurrencyCode(boolean showcurrencyCode) {
    this.showcurrencyCode = showcurrencyCode;
    updateView();
  }
  
  public void setPrefix(final String prefix) {
    this.prefix = prefix + Constants.CHAR_HAIR_SPACE;
    updateView();
  }
  
  public void setAmount(BigMoney amount) {
    this.amount = amount;
    updateView();
  }
  
  private void updateView() {
    if (amount != null) {
      Editable text = new SpannableStringBuilder(
              showcurrencyCode ? mfbCurrencyCode.print(amount) : mfbNoCurrencyCode.print(amount));
      if (prefix != null) {
        text.insert(0, prefix);
      }
      setText(text);
    }
  }
}
