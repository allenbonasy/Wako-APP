package com.pamsillah.wakho;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.common.collect.Lists;
import com.pamsillah.wakho.Models.PostsByAgent;
import com.pamsillah.wakho.Parser.PostsByAgentParser;
import com.pamsillah.wakho.Utils.DTransUrls;
import com.pamsillah.wakho.app_settings.AuthHeader;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by psillah on 3/24/2017.
 */
public class AgentsFragment extends Fragment implements SearchView.OnQueryTextListener {
    private RecyclerView recyclerView;
    private TextView emptyState;
    List<PostsByAgent> data = new ArrayList<>();
    FloatingActionButton fab;
    SwipeRefreshLayout srl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.agents_activity, container, false);
        setHasOptionsMenu(true);

        fab = v.findViewById(R.id.fabagents);
        srl = v.findViewById(R.id.agentSwipeContainer);
        emptyState = v.findViewById(R.id.txtEmptyState);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.getinstance().getSession().getAgent() != null) {
                    if (MyApplication.getinstance().getSession().getAgent()
                            .getCompanyRegNumber() != null && MyApplication.getinstance()
                            .getSession().getAgent().getCompanyRegNumber().contains("Active")) {

                        startActivity(new Intent(getContext(), AgentJourney.class));
                    } else {
                        Toast.makeText(getContext(), "Agent not verified",
                                Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(getContext(), "Register as an agent to post a Journey",
                            Toast.LENGTH_SHORT).show();

                }
            }
        });

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((RecyclerViewAdapter) recyclerView.getAdapter()).clear();
                fill_with_data();
                srl.setRefreshing(false);
            }
        });

        srl.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary,
                R.color.BackgroundDoveGrey);


        recyclerView = v.findViewById(R.id.my_recycler_view);
        fill_with_data();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return v;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchMeuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) searchMeuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
    }


    public void fill_with_data() {


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                DTransUrls.PostJourney, null, new Response.Listener<JSONArray>() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onResponse(JSONArray response) {
                List<PostsByAgent> dat1 = new ArrayList<>();
                dat1.clear();
                dat1 = PostsByAgentParser.parseFeed(response);
                    showEmptyState(dat1.isEmpty());

                for (PostsByAgent item : dat1
                        ) {

                    Date da1 = new Date(), da2 = new Date();
                    String d2 = item.getDepatureDate().split("@")[0].trim();
                    String d1 = new SimpleDateFormat("dd/MM/yy").format(new Date());
                    try {
                        da1 = new SimpleDateFormat("dd/MM/yy").parse(d1);
                        da2 = new SimpleDateFormat("dd/MM/yy").parse(d2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (da2.after(da1) || d2.equals(d1)) {
                            data.add(item);
                        }


                    } catch (Exception ignored) {
                    }

                }
                if (data.size() > 0) {

                    recyclerView.setAdapter(new RecyclerViewAdapter(Lists.reverse(data),
                            getActivity()));
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error : ", error.toString());
                //progressDialog.cancel();

            }
        }) {
            public java.util.Map<String, String> getHeaders() {
                return AuthHeader.getHeaders();
            }
        };


        MyApplication.getinstance().addToRequestQueue(jsonArrayRequest);

    }

    private void showEmptyState(boolean b) {
        if(b){
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        s = s.toLowerCase();
        ArrayList<PostsByAgent> newlist = new ArrayList<>();
        for (PostsByAgent name : data) {
            String to = name.getLocationTo().toLowerCase();
            String from = name.getLocationFrom().toLowerCase();
            String agentName = name.agent.getCompanyName().toLowerCase();

            if (agentName.contains(s) || to.contains(s) || from.contains(s)) {
                newlist.add(name);

            }
        }

        RecyclerView.Adapter newAdapter;
        newAdapter = new RecyclerViewAdapter(newlist, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(newAdapter);
        newAdapter.notifyDataSetChanged();

        return true;

    }
}

