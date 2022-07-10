package com.cr7.budgetapp;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class Root extends Fragment {
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflating the layout file
        View view = inflater.inflate(R.layout.fragment_root, container, false);
        // Referencing to the TabLayout and viewpager
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        // Creating an object of class ViewPagerAdapter which we created
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        // Adding the fragments to the adapter
        viewPagerAdapter.addFragment(new User(), "");
        viewPagerAdapter.addFragment(new OtherUsers(), "");
        // Setting the adapter of the ViewPager as the object (of class ViewPagerAdapter) we
        // created above
        viewPager.setAdapter(viewPagerAdapter);
        // Setting the TabLayout to use the ViewPager. This will allow the swipe functionality to
        // view the next/previous tab
        tabLayout.setupWithViewPager(viewPager);
        // Setting the icon for the tabs at different positions
        tabLayout.getTabAt(0 ).setIcon(R.drawable.budget_vector);
        tabLayout.getTabAt(1 ).setIcon(R.drawable.users_vector);
        // This method is called if the tab is changed/selected either by clicking the tab or if
        // a tab is changed/selected by swiping
        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        // Creating the color for the selected tab
                        int tabIconColor = ContextCompat.getColor(getActivity().getApplicationContext(), R.color.tabSelectedIconColor);
                        // Setting the above created color for the the icon
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        // Creating the color for the unselected tab
                        int tabIconColor = ContextCompat.getColor(getActivity().getApplicationContext(), R.color.tabUnselectedIconColor);
                        // Setting the above created color for the the icon
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
        return view;
    }
}
