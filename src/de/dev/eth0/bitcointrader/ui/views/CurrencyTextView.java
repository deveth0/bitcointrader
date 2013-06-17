package de.dev.eth0.bitcointrader.ui.views;


import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import org.joda.money.BigMoney;
import org.joda.money.format.MoneyAmountStyle;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;

public class CurrencyTextView extends TextView {

  private BigMoney amount = null;
  private static final MoneyFormatter mfb = new MoneyFormatterBuilder().appendCurrencyCode().appendLiteral(" ")
          .appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter();


  public CurrencyTextView(Context context) {
    super(context);
  }

  public CurrencyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setAmount(BigMoney amount) {
    this.amount = amount;
    updateView();
  }

  private void updateView() {
    if (amount != null) {
      
      setText(new SpannableStringBuilder(mfb.print(amount)));
    }
  }
}
