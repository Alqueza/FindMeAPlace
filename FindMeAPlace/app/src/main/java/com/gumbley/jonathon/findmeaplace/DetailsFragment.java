package com.gumbley.jonathon.findmeaplace;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DetailsFragment extends Fragment {

    private PlaceItem mItem;
    private OnFragmentInteractionListener mListener;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.mapPlace);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), MapsActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(getString(R.string.ITEM_LOCATION), mItem.getLocation());
                b.putString(getString(R.string.ITEM_NAME), mItem.getTitle());
                i.putExtra(getString(R.string.LOCATIONS_BUNDLE), b);
                startActivity(i);
            }
        });

        View itemView = v.findViewById(R.id.itemDetails);

        if (mItem != null) {
            ImageView i = (ImageView) itemView.findViewById(R.id.detailsImage);
            if (mItem.getImageUrl() != null && !mItem.getImageUrl().isEmpty())
                Picasso.with(getContext()).load(mItem.getImageUrl()).into(i);

            TextView h = (TextView) itemView.findViewById((R.id.detailsHeading));
            h.setText(mItem.getTitle());

            TextView a = (TextView) itemView.findViewById((R.id.detailsAddress));
            a.setText(mItem.getAddress());

            TextView d = (TextView) itemView.findViewById(R.id.detailsDistance);
            d.setText(mItem.getTitle() + " " + getString(R.string.is) + " " +
                    (mItem.getDistanceFrom() > 1999 ?
                            (Math.round(mItem.getDistanceFrom()/1000) + getContext().getString(R.string.unit_km)) :
                            (Math.round(mItem.getDistanceFrom()) + getContext().getString(R.string.unit_m))) +
                    " " + getContext().getString(R.string.away_from_you));
        }

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setItem(PlaceItem item) {
        mItem = item;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
