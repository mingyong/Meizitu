package me.scola.picrawler.app;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.support.v4.app.Fragment;
import android.widget.Toast;
import me.scola.picrawler.App;
import me.scola.picrawler.data.RequestManager;

/**
 * Created by Sam on 14-3-25.
 */
public class BaseFragment extends Fragment {
    protected final static int BULK_INSERT_MAX_LENGHT = 100;

    @Override
    public void onStop() {
        super.onStop();
        RequestManager.cancelAll(this);
    }

    public void scrollTopAndRefresh(){}

    protected void executeRequest(Request request) {
        RequestManager.addRequest(request, this);
    }

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(App.getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    protected boolean isInserting() {
        return false;
    }
}
