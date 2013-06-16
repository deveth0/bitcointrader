package de.dev.eth0.bitcointrader.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.model.Order;
import de.dev.eth0.bitcointrader.util.ViewPagerTabs;

public class OrderFragment extends Fragment {

  private static final String TAG = OrderFragment.class.getSimpleName();
  private static final int INITIAL_PAGE = 1;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, ".onCreate()");
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    Log.d(TAG, ".onCreateView()");
    final View view = inflater.inflate(R.layout.order_fragment, container, false);

    final ViewPagerTabs pagerTabs = (ViewPagerTabs)view.findViewById(R.id.order_pager_tabs);
    pagerTabs.addTabLabels(R.string.bitcoin_order_tab_bid, R.string.bitcoin_order_tab_both,
            R.string.bitcoin_order_tab_ask);

    final PagerAdapter pagerAdapter = new PagerAdapter(getFragmentManager());

    final ViewPager pager = (ViewPager)view.findViewById(R.id.transactions_pager);
    pager.setAdapter(pagerAdapter);
    pager.setOnPageChangeListener(pagerTabs);
    pager.setCurrentItem(INITIAL_PAGE);
    pager.setPageMargin(2);
    pager.setPageMarginDrawable(R.color.bg_less_bright);
    pagerTabs.onPageScrolled(INITIAL_PAGE, 0, 0); // should not be needed

    return view;
  }

  private static class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(final FragmentManager fm) {
      super(fm);
    }

    @Override
    public int getCount() {
      return 3;
    }

    @Override
    public Fragment getItem(final int position) {
      Order.OrderType orderType;
      if (position == 0) {
        orderType = Order.OrderType.BID;
      }
      else if (position == 1) {
        orderType = null;
      }
      else if (position == 2) {
        orderType = Order.OrderType.ASK;
      }
      else {
        throw new IllegalStateException();
      }

      return OrderListFragment.instance(orderType);
    }
  }
}
