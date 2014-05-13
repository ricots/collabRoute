package it.raffaeletosti.collabroute;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import it.raffaeletosti.collabroute.model.Travel;
import it.raffaeletosti.collabroute.model.UserHandler;


public class TravelActivity extends FragmentActivity {

    ViewPager mViewPager;
    MyPagerAdapter mViewPagerAdapter;
    protected static Travel travel;
    static UserHandler user;
    protected static GMapFragment map;
    protected static ChatFragment chat;
    protected static RoutesFragment route;
    protected static UsersFragment users;
    private Dialog exitTravel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        String travelString = getIntent().getExtras().getString("travel");
        travel = new Travel();
        travel.createFromJSONString(travelString);
        setTitle(travel.getName());
        user = getIntent().getParcelableExtra("user");
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
            }
        });
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mViewPagerAdapter);
        ActionBar.TabListener tabListener = new android.app.ActionBar.TabListener() {
            @Override
            public void onTabSelected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };
        Resources res = getResources();
        String packageName = getPackageName();
        for(int i = 0; i < 4; i++){
            int id = res.getIdentifier("travel_tab_"+i, "string", packageName);
            actionBar.addTab(actionBar.newTab()
            .setText(res.getText(id))
            .setTabListener(tabListener));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.travel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createExitTravelDialog(){
        exitTravel = new Dialog(this);
        exitTravel.setContentView(R.layout.exit_travel_dialog);
        exitTravel.setTitle(getString(R.string.exit_travel_title));
        final Button exitOk = (Button) exitTravel.findViewById(R.id.exitOk);
        final Button exitCancel = (Button) exitTravel.findViewById(R.id.exitCancel);
        exitOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTravelActivity();
            }
        });
        exitCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitTravel.dismiss();
            }
        });
    }

    private void closeTravelActivity() {
        if(exitTravel.isShowing()){
            exitTravel.dismiss();
        }
        final Intent intent = new Intent(this, travelListActivity.class);
        startActivityForResult(intent, RESULT_OK);
        closeEverything();
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            createExitTravelDialog();
            exitTravel.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (map == null) {
                        map = GMapFragment.newInstance();
                    }
                    return map;
                case 1:
                    if (route == null) {
                        route = RoutesFragment.newInstance();
                    }
                    return route;
                case 2:
                    if (chat == null) {
                        chat = ChatFragment.newInstance();
                    }
                    return chat;
                case 3:
                    if (users == null) {
                        users = UsersFragment.newInstance();
                    }
                    return users;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    public static void closeEverything(){
        map.view = null;
        map.map = null;
        map.client.disconnect();
        map.locationManager.removeUpdates(map);
        map = null;
        route = null;
        chat = null;
    }
}
