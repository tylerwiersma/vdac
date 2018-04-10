package trikita.talalarmo.alarm;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import trikita.jedux.Action;
import trikita.jedux.Store;
import trikita.talalarmo.GsonAdaptersState;
import trikita.talalarmo.ImmutableState;
import trikita.talalarmo.State;

public class PersistanceController implements Store.Middleware<Action, State> {

    private final SharedPreferences mPreferences;
    private final Gson mGson;

    /**
     * Controls the persistance between sessions
     * @param context
     */
    public PersistanceController(Context context) {
        mPreferences = context.getSharedPreferences("data", 0);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersState());
        mGson = gsonBuilder.create();
    }

    /**
     * Retreives saved state
     * @return
     */
    public State getSavedState() {
        if (mPreferences.contains("data")) {
            String json = mPreferences.getString("data", "");
            return mGson.fromJson(json, ImmutableState.class);
        }
        return null;
    }

    /**
     * Dispatch action
     * @param store
     * @param action
     * @param next
     */
    @Override
    public void dispatch(Store<Action, State> store, Action action, Store.NextDispatcher<Action> next) {
        next.dispatch(action);
        String json = mGson.toJson(store.getState());
        mPreferences.edit().putString("data", json).apply();
    }
}
