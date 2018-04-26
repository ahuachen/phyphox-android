package de.rwth_aachen.phyphox;


import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class expViewFragment extends Fragment {
    private static final String ARG_INDEX = "index";

    private int index;
    public ScrollView root;

    public expViewFragment() {
        // Required empty public constructor
    }

    public int getGraphCount() {
        int n = 0;
        for (expView.expViewElement element : ((Experiment) getActivity()).experiment.experimentViews.elementAt(index).elements) {
            if (element.getClass() == expView.graphElement.class)
                n++;
        }
        return n;
    }

    //Apply zoom to all graphs on the current page.
    public void applyZoom(double min, double max, boolean follow, String unit, String buffer, boolean yAxis) {
        for (expView.expViewElement element : ((Experiment) getActivity()).experiment.experimentViews.elementAt(index).elements) {
            if (element.getClass() == expView.graphElement.class) {
                expView.graphElement ge = (expView.graphElement)element;
                ge.applyZoom(min, max, follow, unit, buffer, yAxis);
            }
        }
    }

    public void requestExclusive(expView.expViewElement caller) {
        if (root == null)
            return;
        root.setFillViewport(true);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(150);
        layoutTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        layoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            layoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        }
        LinearLayout ll = (LinearLayout)root.findViewById(R.id.experimentView);
        ll.setLayoutTransition(layoutTransition);
        for (expView.expViewElement element : ((Experiment) getActivity()).experiment.experimentViews.elementAt(index).elements) {
            if (element == caller) {
                element.maximize();
            } else {
                element.hide();
            }
        }
        ll.setLayoutTransition(null);
    }

    public void leaveExclusive() {
        if (root == null)
            return;
        root.setFillViewport(false);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(150);
        layoutTransition.setStartDelay(LayoutTransition.APPEARING, 0);
        layoutTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            layoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        }
        LinearLayout ll = (LinearLayout)root.findViewById(R.id.experimentView);
        ll.setLayoutTransition(layoutTransition);
        for (expView.expViewElement element : ((Experiment) getActivity()).experiment.experimentViews.elementAt(index).elements) {
            element.restore();
        }
        ll.setLayoutTransition(null);
    }

    public static expViewFragment newInstance(int index) {
        expViewFragment fragment = new expViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_INDEX);
        }
    }

    public void recreateView() {
        if (root == null)
            return;
        LinearLayout ll = (LinearLayout)root.findViewById(R.id.experimentView);
        ll.setMinimumHeight(root.getHeight()-2*getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        ll.removeAllViews();

        if (((Experiment)getActivity()).experiment != null && ((Experiment)getActivity()).experiment.experimentViews.size() > index) {
            for (expView.expViewElement element : ((Experiment) getActivity()).experiment.experimentViews.elementAt(index).elements) {
                element.createView(ll, getContext(), getResources(), this);
            }
        }

        if (((Experiment)getActivity()).experiment != null)
            ((Experiment) getActivity()).experiment.updateViews(index, true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if (getActivity() != null && ((Experiment)getActivity()).experiment != null)
                ((Experiment) getActivity()).experiment.updateViews(index, true);
        } else {
            leaveExclusive();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = (ScrollView)inflater.inflate(R.layout.fragment_exp_view, container, false);

        final LinearLayout ll = (LinearLayout)root.findViewById(R.id.experimentView);
        ll.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ll.getWindowToken(), 0);
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        recreateView();
        super.onStart();
    }

    @Override
    public void onStop() {
        if (root == null)
            return;
        root.setFillViewport(false);
        LinearLayout ll = (LinearLayout)root.findViewById(R.id.experimentView);
        ll.removeAllViews();

        if (((Experiment)getActivity()).experiment != null && ((Experiment)getActivity()).experiment.experimentViews.size() > index) {
            for (expView.expViewElement element : ((Experiment) getActivity()).experiment.experimentViews.elementAt(index).elements) {
                element.cleanView();
            }
        }

        super.onStop();
    }

}
