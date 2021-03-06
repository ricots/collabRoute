package it.raffaeletosti.collabroute;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.raffaeletosti.collabroute.travels.TravelContent;

/**
 * A fragment representing a single travel detail screen.
 * This fragment is either contained in a {@link it.raffaeletosti.collabroute.travelListActivity}
 * in two-pane mode (on tablets) or a {@link it.raffaeletosti.collabroute.travelDetailActivity}
 * on handsets.
 */
public class travelDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The Travel content this fragment is presenting.
     */
    private TravelContent.TravelItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public travelDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the Travel content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = TravelContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_travel_detail, container, false);
        return rootView;
    }
}
