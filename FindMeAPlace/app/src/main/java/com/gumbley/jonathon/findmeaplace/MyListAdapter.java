package com.gumbley.jonathon.findmeaplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MyListAdapter extends ArrayAdapter<PlaceItem> {

    public MyListAdapter(Context context, int resource, List<PlaceItem> items){
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.list_item, parent, false);

        PlaceItem item = getItem(position);

        if (item != null){
            ImageView i = (ImageView) v.findViewById(R.id.listImage);
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty())
                Picasso.with(getContext()).load(item.getImageUrl()).into(i);

            TextView h = (TextView) v.findViewById(R.id.itemHeading);
            h.setText(item.getTitle());
            TextView d = (TextView) v.findViewById(R.id.itemDescription);
            d.setText(getContext().getString(R.string.you_are) + " " +
                    (item.getDistanceFrom() > 1999 ?
                            (Math.round(item.getDistanceFrom() / 1000) + getContext().getString(R.string.unit_km)) :
                            (Math.round(item.getDistanceFrom()) + getContext().getString(R.string.unit_m))) +
                    " " + getContext().getString(R.string.away));
        }

        return v;
    }
}
