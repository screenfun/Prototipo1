package com.example.cristhian.prototipo2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cristhian.prototipo2.util.SystemUiHider;

import java.io.File;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Splash extends Activity {

    /**
     * Fuente Roboto
     */
    Typeface roboto;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.tituloBus);

        //mostrar el logo
        ImageView logoLocale = (ImageView) findViewById(R.id.logoLocale);
        logoLocale.setVisibility(View.VISIBLE);


        //poner fuente al titulo
        TextView titulo = (TextView) findViewById(R.id.tituloBus);
        roboto = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        titulo.setTypeface(roboto);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


        animacionLogo();
        titulo.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha));
        delayScreenSplash();

    }


    private void animacionLogo() {

        final ImageView myImage = (ImageView) findViewById(R.id.logoLocale);
        final Animation myRotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.iniciar);
        myImage.startAnimation(myRotation);
    }


    private void delayScreenSplash() {

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */

                BaseDeDatos baseDeDatos = new BaseDeDatos(Splash.this.getBaseContext());
                baseDeDatos.abrir();

                Cursor datosUsuario = baseDeDatos.getDatosUsuario();

                if(datosUsuario == null){
                    Intent mainIntent = new Intent(Splash.this, Inicio.class);
                    Splash.this.startActivity(mainIntent);
                    Splash.this.finish();
                    return;
                }


                datosUsuario.moveToFirst();
                if(datosUsuario.getString(datosUsuario.getColumnIndexOrThrow("actualizacion")).equals("si")){
                    baseDeDatos.setNoActualizacion(datosUsuario.getString(datosUsuario.getColumnIndexOrThrow("actualizacion")));
                    new Copia().copiarDatos(Splash.this.getBaseContext(),
                            datosUsuario.getString(datosUsuario.getColumnIndexOrThrow("usuario")));
                    //por si hay cambios en la tabla usuario
                    datosUsuario = baseDeDatos.getDatosUsuario();

                }


                String recientes = baseDeDatos.getRecientes(
                        datosUsuario.getString(
                                datosUsuario.getColumnIndexOrThrow("usuario")
                        )
                );


                String paraderos = baseDeDatos.getParaderos();

                Intent activityLugares = new Intent(Splash.this, Lugares.class);
                activityLugares.putExtra("datosUsuario" , (Parcelable) datosUsuario);
                activityLugares.putExtra("lugaresrecientes" , recientes);
                activityLugares.putExtra("paraderos" , paraderos);

                baseDeDatos.cerrar();

                Splash.this.startActivity(activityLugares);
                overridePendingTransition(R.anim.zoom_forward_in, R.anim.zoom_forward_out);
                Splash.this.finish();
            }
        }, 4000);

    }

    private void borrarBd() {
        File database = getApplicationContext().getDatabasePath("stopbus.db");
        Log.i("Database", database.getFreeSpace() + "");

        if (!database.exists()) {
            // Database does not exist so copy it from assets here
            Log.i("Database", "no encontrada");
        } else {
            Log.i("Database", "encontrada");
            database.delete();
            Log.i("Database", "borrada");
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

}
