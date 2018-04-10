package trikita.talalarmo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Window;
import android.view.WindowManager;

import trikita.anvil.Anvil;
import trikita.anvil.RenderableView;
import trikita.promote.Promote;
import trikita.talalarmo.ui.AlarmLayout;
import trikita.talalarmo.ui.Theme;

public class MainActivity extends Activity {

    static final int REQUEST_PERMISSION_KEY = 1;

    /**
     * When app launches this runs
     * @param b
     */
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        updateTheme();


        String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
        if(!Function.hasPermissions(this, PERMISSIONS)){
          ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }


        setContentView(new RenderableView(this) {
            public void view() {
                AlarmLayout.view();
            }
        });
    }

    /**
     * Default app promotional stuffs. Was open source so who knows who is going to get an add.
     */
    public void onResume() {
        super.onResume();
        updateTheme();
        Anvil.render();
        Promote.after(7).days().every(7).days().rate(this);
        Promote.after(3).days().every(14).days().share(this,
                Promote.FACEBOOK_TWITTER,
                "https://github.com/trikita/talalarmo",
                "Talalarmo: elegant open-source alarm clock");
    }

    /**
     * Settings menu code
     */
    public void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Theme code
     */
    private void updateTheme() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (Theme.get(App.getState().settings().theme()).light) {
                setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
            } else {
                setTheme(android.R.style.Theme_Holo_NoActionBar);
            }
        } else {
            if (Theme.get(App.getState().settings().theme()).light) {
                setTheme(android.R.style.Theme_Material_Light_NoActionBar);
            } else {
                setTheme(android.R.style.Theme_Material_NoActionBar);
            }
        }

        // fill status bar with a theme dark color on post-Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Theme.get(App.getState().settings().theme()).primaryDarkColor);
        }
    }

}
