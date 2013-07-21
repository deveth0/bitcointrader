
package com.veken0m.bitcoinium.exchanges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.veken0m.bitcoinium.BaseExchangeFragment;
import com.veken0m.bitcoinium.R;

public class Bitcoin24Fragment extends BaseExchangeFragment {

    public Bitcoin24Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.menu_fragment, container, false);
        buildMenu(view, BITCOIN24, false);
        return view;
    }

}
